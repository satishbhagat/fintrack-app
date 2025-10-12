package com.fintrack.client.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.fintrack.client.R;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.dto.SpendDataResponse;
import com.fintrack.client.models.AddCreditCardRequest;
import com.fintrack.client.models.AddFixedExpenditureRequest;
import com.fintrack.client.models.ExtraIncome;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SpendActivity extends BaseActivity {

    private LinearLayout containerFixedExpenses, containerCreditCards;
    private Button buttonAddExpense, buttonAddCard;
    private ImageButton buttonAddOtherIncome;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spend);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setToolbarTitle("Manage Spending"); // Set the title for the toolbar

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        containerFixedExpenses = findViewById(R.id.containerFixedExpenses);
        containerCreditCards = findViewById(R.id.containerCreditCards);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonAddCard = findViewById(R.id.buttonAddCard);
        buttonAddOtherIncome = findViewById(R.id.buttonAddOtherIncome);

        buttonAddExpense.setOnClickListener(v -> showAddFixedExpenseDialog());
        buttonAddCard.setOnClickListener(v -> showAddCreditCardDialog());
        buttonAddOtherIncome.setOnClickListener(v -> showAddOtherIncomeDialog());

        fetchSpendData();
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void fetchSpendData() {
        UUID userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getSpendData(String.valueOf(userId)).enqueue(new Callback<SpendDataResponse>() {
            @Override
            public void onResponse(Call<SpendDataResponse> call, Response<SpendDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateSpendData(response.body());
                } else {
                    Toast.makeText(SpendActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SpendDataResponse> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateSpendData(SpendDataResponse data) {
        // Populate Fixed Expenses
        containerFixedExpenses.removeAllViews();
        List<SpendDataResponse.FixedExpenditureItem> fixedExpenses = data.getFixedExpenditures();
        if (fixedExpenses != null) {
            for (SpendDataResponse.FixedExpenditureItem item : fixedExpenses) {
                addTextViewToShow(containerFixedExpenses, item.getName() + " - ₹" + item.getAmount());
            }
        }

        // Populate Credit Cards
        containerCreditCards.removeAllViews();
        List<SpendDataResponse.CreditCardItem> creditCards = data.getCreditCards();
        if (creditCards != null) {
            for (SpendDataResponse.CreditCardItem item : creditCards) {
                addTextViewToShow(containerCreditCards, item.getCardName() + " (Due: " + item.getDueDate() + ")");
            }
        }
    }

    private void addTextViewToShow(LinearLayout container, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.on_secondary)); // Changed to a slick, theme-aware color
        textView.setTextSize(16);
        textView.setPadding(0, 8, 0, 8);
        container.addView(textView);
    }

    private void showAddFixedExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_fixed_expense, null);
        builder.setView(dialogView);

        final EditText etExpenseName = dialogView.findViewById(R.id.etExpenseName);
        final EditText etExpenseAmount = dialogView.findViewById(R.id.etExpenseAmount);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etExpenseName.getText().toString();
            String amount = etExpenseAmount.getText().toString();
            if (!name.isEmpty() && !amount.isEmpty()) {
                AddFixedExpenditureRequest request = new AddFixedExpenditureRequest();
                request.name = name;
                request.amount = Double.parseDouble(amount);
                request.userId = UserSession.getInstance().getUserId().toString();
                // You might need to add due_date to your dialog if required by backend
                // request.due_date = ...;
                saveNewFixedExpense(request);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddCreditCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_credit_card, null);
        builder.setView(dialogView);

        final EditText etCardName = dialogView.findViewById(R.id.etCardName);
        final EditText etDueDate = dialogView.findViewById(R.id.etDueDate);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etCardName.getText().toString();
            // Assuming due date is also captured in your AddCreditCardRequest
            if (!name.isEmpty()) {
                AddCreditCardRequest request = new AddCreditCardRequest();
                request.cardName = name;
                request.userId = UserSession.getInstance().getUserId().toString();
                request.dueDate = Integer.parseInt(etDueDate.getText().toString()); // Add due date if applicable
                saveNewCreditCard(request);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddOtherIncomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_other_income, null);
        builder.setView(dialogView);

        final EditText etDesc = dialogView.findViewById(R.id.etIncomeDescription);
        final EditText etAmount = dialogView.findViewById(R.id.etIncomeAmount);
        final RadioGroup rgType = dialogView.findViewById(R.id.rgIncomeType);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String desc = etDesc.getText().toString();
            String amount = etAmount.getText().toString();
            if (!desc.isEmpty() && !amount.isEmpty()) {
                ExtraIncome income = new ExtraIncome();
                income.setDescription(desc);
                income.setAmount(Double.parseDouble(amount));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                income.setIncomeMonth(sdf.format(Calendar.getInstance().getTime()));

                int selectedId = rgType.getCheckedRadioButtonId();
                RadioButton radioButton = dialogView.findViewById(selectedId);
                income.setRecurring("Recurring".equals(radioButton.getText().toString()));

                saveOtherIncome(income);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // API Saving methods
    private void saveNewFixedExpense(AddFixedExpenditureRequest request) {
        apiService.addFixedExpense(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Bill Saved!", Toast.LENGTH_SHORT).show();
                    fetchSpendData(); // Refresh list
                } else {
                    Toast.makeText(SpendActivity.this, "Failed to save.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNewCreditCard(AddCreditCardRequest request) {
        apiService.addCreditCard(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Card Saved!", Toast.LENGTH_SHORT).show();
                    fetchSpendData(); // Refresh list
                } else {
                    Toast.makeText(SpendActivity.this, "Failed to save.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOtherIncome(ExtraIncome income) {
        apiService.addExtraIncome(income).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Income Logged!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SpendActivity.this, "Failed to log income.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.getMenu().findItem(R.id.nav_spend).setChecked(true);
    }
}


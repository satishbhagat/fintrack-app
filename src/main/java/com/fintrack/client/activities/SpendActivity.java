package com.fintrack.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.fintrack.client.R;
import com.fintrack.client.dto.IncomeResponse;
import com.fintrack.client.dto.SpendDataResponse;
import com.fintrack.client.models.*;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class SpendActivity extends BaseActivity {

    private LinearLayout containerFixedExpenses, containerCreditCards, containerOtherIncome;
    private Button buttonAddExpense, buttonAddCard, buttonAddOtherIncome;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spend);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setToolbarTitle("Manage Spending");

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);
        // Add listener to handle Goal navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_goals) {
                // Navigate to GoalTrackingFragment or Activity
                // If it's a fragment within DashboardActivity:
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("NAVIGATE_TO", "goals"); // Add extra to signal navigation
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Bring DashboardActivity to front if exists
                startActivity(intent);
                return true;
            }
            // Handle other navigation items using the BaseActivity's method
            return super.onNavigationItemSelected(item);
        });


        apiService = RetrofitClient.getInstance().create(ApiService.class);

        containerFixedExpenses = findViewById(R.id.containerFixedExpenses);
        containerCreditCards = findViewById(R.id.containerCreditCards);
        containerOtherIncome = findViewById(R.id.containerOtherIncome);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonAddCard = findViewById(R.id.buttonAddCard);
        buttonAddOtherIncome = findViewById(R.id.buttonAddOtherIncome);

        buttonAddExpense.setOnClickListener(v -> showAddFixedExpenseDialog(null));
        buttonAddCard.setOnClickListener(v -> showAddCreditCardDialog(null));
        buttonAddOtherIncome.setOnClickListener(v -> showAddOtherIncomeDialog(null));

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
            // Optional: Redirect to login
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
        containerFixedExpenses.removeAllViews();
        if (data.getFixedExpenditures() != null) {
            for (SpendDataResponse.FixedExpenditureItem item : data.getFixedExpenditures()) {
                addEditableItemView(containerFixedExpenses, item.getName() + " - ₹" + item.getAmount(), item, "fixed");
            }
        }

        containerCreditCards.removeAllViews();
        if (data.getCreditCards() != null) {
            for (SpendDataResponse.CreditCardItem item : data.getCreditCards()) {
                addEditableItemView(containerCreditCards, item.getCardName() + " (Due: " + item.getDueDate() + ")", item, "card");
            }
        }

        containerOtherIncome.removeAllViews();
        if (data.getExtraIncome() != null) {
            for (SpendDataResponse.ExtraIncomeItem item : data.getExtraIncome()) {
                addEditableItemView(containerOtherIncome, item.getDescription() + " - ₹" + item.getAmount(), item, "income");
            }
        }
    }

    private void addEditableItemView(LinearLayout container, String text, final Object item, final String type) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.list_item_editable, container, false);

        TextView itemText = itemView.findViewById(R.id.item_text);
        ImageButton editButton = itemView.findViewById(R.id.edit_button);
        ImageButton deleteButton = itemView.findViewById(R.id.delete_button);

        itemText.setText(text);

        editButton.setOnClickListener(v -> {
            if ("fixed".equals(type)) {
                showAddFixedExpenseDialog((SpendDataResponse.FixedExpenditureItem) item);
            } else if ("card".equals(type)) {
                showAddCreditCardDialog((SpendDataResponse.CreditCardItem) item);
            } else if ("income".equals(type)) {
                showAddOtherIncomeDialog((SpendDataResponse.ExtraIncomeItem) item);
            }
        });

        deleteButton.setOnClickListener(v -> {
            String itemId = null;
            if ("fixed".equals(type)) {
                itemId = ((SpendDataResponse.FixedExpenditureItem) item).getId();
            } else if ("card".equals(type)) {
                itemId = ((SpendDataResponse.CreditCardItem) item).getId();
            } else if ("income".equals(type)) {
                itemId = ((SpendDataResponse.ExtraIncomeItem) item).getId();
            }

            if (itemId == null) {
                Toast.makeText(SpendActivity.this, "Error: Item ID is missing.", Toast.LENGTH_SHORT).show();
                return;
            }

            final String finalItemId = itemId; // Need final variable for lambda

            new AlertDialog.Builder(this)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if ("fixed".equals(type)) {
                            deleteFixedExpense(finalItemId);
                        } else if ("card".equals(type)) {
                            deleteCreditCard(finalItemId);
                        } else if ("income".equals(type)) {
                            deleteOtherIncome(finalItemId);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        container.addView(itemView);
    }


    // --- Dialog Methods (showAddFixedExpenseDialog, showAddCreditCardDialog, showAddOtherIncomeDialog) ---
    private void showAddFixedExpenseDialog(SpendDataResponse.FixedExpenditureItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_fixed_expense, null);
        builder.setView(dialogView);

        final EditText etExpenseName = dialogView.findViewById(R.id.etExpenseName);
        final EditText etExpenseAmount = dialogView.findViewById(R.id.etExpenseAmount);

        builder.setTitle(item == null ? "Add New Bill or EMI" : "Edit Bill or EMI");
        if (item != null) {
            etExpenseName.setText(item.getName());
            etExpenseAmount.setText(item.getAmount().toString());
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etExpenseName.getText().toString();
            String amount = etExpenseAmount.getText().toString();
            if (!name.isEmpty() && !amount.isEmpty()) {
                AddFixedExpenditureRequest request = new AddFixedExpenditureRequest();
                request.setName(name);
                request.setAmount(new BigDecimal(amount));
                request.setUserId(UserSession.getInstance().getUserId().toString());
                // Assuming dueDate handling if needed based on backend
                // request.setDueDate(...);
                if (item == null) {
                    saveNewFixedExpense(request);
                } else {
                    updateFixedExpense(item.getId(), request);
                }
            } else {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddCreditCardDialog(SpendDataResponse.CreditCardItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_credit_card, null);
        builder.setView(dialogView);

        final EditText etCardName = dialogView.findViewById(R.id.etCardName);
        final EditText etDueDate = dialogView.findViewById(R.id.etDueDate);

        builder.setTitle(item == null ? "Add New Credit Card" : "Edit Credit Card");
        if(item != null) {
            etCardName.setText(item.getCardName());
            etDueDate.setText(item.getDueDate() != null ? item.getDueDate() : "");
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etCardName.getText().toString();
            String dueDateStr = etDueDate.getText().toString();
            if (!name.isEmpty() && !dueDateStr.isEmpty()) {
                try {
                    int dueDate = Integer.parseInt(dueDateStr);
                    if (dueDate < 1 || dueDate > 31) {
                        Toast.makeText(this, "Due date must be between 1 and 31.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AddCreditCardRequest request = new AddCreditCardRequest();
                    request.setCardName(name);
                    request.setUserId(UserSession.getInstance().getUserId().toString());
                    request.setDueDate(dueDate);
                    if (item == null) {
                        saveNewCreditCard(request);
                    } else {
                        updateCreditCard(item.getId(), request);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid due date.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddOtherIncomeDialog(SpendDataResponse.ExtraIncomeItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_other_income, null);
        builder.setView(dialogView);

        final EditText etDesc = dialogView.findViewById(R.id.etIncomeDescription);
        final EditText etAmount = dialogView.findViewById(R.id.etIncomeAmount);
        final RadioGroup rgType = dialogView.findViewById(R.id.rgIncomeType);
        final RadioButton rbOneTime = dialogView.findViewById(R.id.rbOneTime);
        final RadioButton rbRecurring = dialogView.findViewById(R.id.rbRecurring);

        builder.setTitle(item == null ? "Log Other Income" : "Edit Other Income");
        if (item != null) {
            etDesc.setText(item.getDescription());
            etAmount.setText(item.getAmount().toString());
            if (item.isRecurring()) {
                rbRecurring.setChecked(true);
            } else {
                rbOneTime.setChecked(true);
            }
        } else {
            rbOneTime.setChecked(true); // Default for new income
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String desc = etDesc.getText().toString();
            String amountStr = etAmount.getText().toString();
            if (!desc.isEmpty() && !amountStr.isEmpty()) {
                AddIncomeRequest income = new AddIncomeRequest();
                income.setDescription(desc);
                income.setAmount(new BigDecimal(amountStr));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                income.setIncomeMonth(sdf.format(Calendar.getInstance().getTime())); // Consider using a date picker
                income.setUserId(UserSession.getInstance().getUserId().toString());
                income.setRecurring(rbRecurring.isChecked());

                if (item == null) {
                    saveOtherIncome(income);
                } else {
                    updateOtherIncome(item.getId(), income);
                }
            } else {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // --- API Call Methods (saveNewFixedExpense, updateFixedExpense, deleteFixedExpense, etc.) ---
    private void saveNewFixedExpense(AddFixedExpenditureRequest request) {
        apiService.addFixedExpense(request).enqueue(new Callback<FixedExpenditure>() { // Changed from GenericResponse
            @Override
            public void onResponse(Call<FixedExpenditure> call, Response<FixedExpenditure> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Bill Saved!", Toast.LENGTH_SHORT).show();
                    fetchSpendData();
                } else {
                    Toast.makeText(SpendActivity.this, "Failed to save bill.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<FixedExpenditure> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFixedExpense(String id, AddFixedExpenditureRequest request) {
        apiService.updateFixedExpense(id, request).enqueue(new Callback<FixedExpenditure>() { // Changed
            @Override
            public void onResponse(Call<FixedExpenditure> call, Response<FixedExpenditure> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Bill Updated!", Toast.LENGTH_SHORT).show();
                    fetchSpendData();
                } else {
                    Toast.makeText(SpendActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<FixedExpenditure> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFixedExpense(String id) {
        apiService.deleteFixedExpense(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Bill Deleted!", Toast.LENGTH_SHORT).show();
                    fetchSpendData();
                } else {
                    Toast.makeText(SpendActivity.this, "Delete failed.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNewCreditCard(AddCreditCardRequest request) {
        apiService.addCreditCard(request).enqueue(new Callback<CreditCard>() { // Changed
            @Override
            public void onResponse(Call<CreditCard> call, Response<CreditCard> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Card Saved!", Toast.LENGTH_SHORT).show();
                    fetchSpendData();
                } else {
                    Toast.makeText(SpendActivity.this, "Failed to save card.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<CreditCard> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCreditCard(String id, AddCreditCardRequest request) {
        apiService.updateCreditCard(id, request).enqueue(new Callback<CreditCard>() { // Changed
            @Override
            public void onResponse(Call<CreditCard> call, Response<CreditCard> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Card Updated!", Toast.LENGTH_SHORT).show();
                    fetchSpendData();
                } else {
                    Toast.makeText(SpendActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<CreditCard> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCreditCard(String id) {
        apiService.deleteCreditCard(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Card Deleted!", Toast.LENGTH_SHORT).show();
                    fetchSpendData();
                } else {
                    Toast.makeText(SpendActivity.this, "Delete failed.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOtherIncome(AddIncomeRequest income) {
        apiService.addExtraIncome(income).enqueue(new Callback<IncomeResponse>() { // Changed
            @Override
            public void onResponse(Call<IncomeResponse> call, Response<IncomeResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Income Logged!", Toast.LENGTH_SHORT).show();
                    fetchSpendData();
                } else {
                    Toast.makeText(SpendActivity.this, "Failed to log income.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<IncomeResponse> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOtherIncome(String id, AddIncomeRequest request) {
        apiService.updateExtraIncome(id, request).enqueue(new Callback<IncomeResponse>() { // Changed
            @Override
            public void onResponse(Call<IncomeResponse> call, Response<IncomeResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Income Updated!", Toast.LENGTH_SHORT).show();
                    fetchSpendData();
                } else {
                    Toast.makeText(SpendActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<IncomeResponse> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteOtherIncome(String id) {
        apiService.deleteExtraIncome(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Income Deleted!", Toast.LENGTH_SHORT).show();
                    fetchSpendData();
                } else {
                    Toast.makeText(SpendActivity.this, "Delete failed.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.getMenu().findItem(R.id.nav_spend).setChecked(true);
    }
}

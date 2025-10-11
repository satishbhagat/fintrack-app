package com.fintrack.client.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.fintrack.client.R;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.dto.ProfileSetupRequest;
import com.fintrack.client.models.ExtraIncome;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpendActivity extends BaseActivity {

    private EditText editTextSalary;
    private LinearLayout containerFixedExpenses, containerCreditCards;
    private Button buttonAddExpense, buttonAddCard;
    private ApiService apiService;
    private String emailId;

    // Bonus and Other Income fields
    private EditText etBonusAmount, etOtherIncomeName, etOtherIncomeAmount;
    private TextView tvBonusDate;
    private RadioGroup rgBonusType, rgOtherIncomeType;
    private Calendar bonusCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spend);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        setToolbarTitle("Manage Spending");
        emailId = UserSession.getInstance().getEmailId();
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Initialize views
        editTextSalary = findViewById(R.id.editTextSalary);
        containerFixedExpenses = findViewById(R.id.containerFixedExpenses);
        containerCreditCards = findViewById(R.id.containerCreditCards);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonAddCard = findViewById(R.id.buttonAddCard);

        // Bonus and Other Income
        etBonusAmount = findViewById(R.id.etBonusAmount);
        tvBonusDate = findViewById(R.id.tvBonusDate);
        rgBonusType = findViewById(R.id.rgBonusType); // Corrected ID
        etOtherIncomeName = findViewById(R.id.etOtherIncomeName);
        etOtherIncomeAmount = findViewById(R.id.etOtherIncomeAmount);
        rgOtherIncomeType = findViewById(R.id.rgOtherIncomeType);

        if (UserSession.getInstance().getSalary() != null) {
            editTextSalary.setText(UserSession.getInstance().getSalary().toString());
        }

        buttonAddExpense.setOnClickListener(v -> addFixedExpenseView());
        buttonAddCard.setOnClickListener(v -> addCreditCardView());
        tvBonusDate.setOnClickListener(v -> showDatePicker());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.spend_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addFixedExpenseView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View expenseView = inflater.inflate(R.layout.item_fixed_expense, containerFixedExpenses, false);
        setupSpinnerListener(expenseView, R.id.spinnerExpenseName, R.id.etOtherExpense);
        containerFixedExpenses.addView(expenseView);
    }

    private void addCreditCardView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.item_credit_card, containerCreditCards, false);
        setupSpinnerListener(cardView, R.id.spinnerCardName, R.id.etOtherCreditCard);
        containerCreditCards.addView(cardView);
    }

    private void setupSpinnerListener(View parentView, int spinnerId, int otherEditTextId) {
        Spinner spinner = parentView.findViewById(spinnerId);
        EditText otherEditText = parentView.findViewById(otherEditTextId);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if ("Other".equalsIgnoreCase(selectedItem)) {
                    otherEditText.setVisibility(View.VISIBLE);
                } else {
                    otherEditText.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                otherEditText.setVisibility(View.GONE);
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            bonusCalendar.set(Calendar.YEAR, year);
            bonusCalendar.set(Calendar.MONTH, month);
            bonusCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            tvBonusDate.setText(sdf.format(bonusCalendar.getTime()));
        }, bonusCalendar.get(Calendar.YEAR), bonusCalendar.get(Calendar.MONTH), bonusCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveProfile() {
        saveBonusIncome();
        saveOtherIncome();
        // The rest of the profile saving logic
        String salaryStr = editTextSalary.getText().toString();
        if (salaryStr.isEmpty()) {
            Toast.makeText(this, "Please enter your monthly salary.", Toast.LENGTH_SHORT).show();
            return;
        }
        // ... (rest of the saveProfile logic for salary, fixed expenses, etc.)
        Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private void saveBonusIncome() {
        String amountStr = etBonusAmount.getText().toString();
        if (!amountStr.isEmpty()) {
            ExtraIncome bonus = new ExtraIncome();
            bonus.setAmount(Double.parseDouble(amountStr));
            bonus.setDescription("Bonus");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            bonus.setIncomeMonth(sdf.format(bonusCalendar.getTime()));

            int selectedId = rgBonusType.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            bonus.setRecurring("Recurring".equals(radioButton.getText().toString()));

            // Call API to save bonus
            saveExtraIncome(bonus);
        }
    }

    private void saveOtherIncome() {
        String name = etOtherIncomeName.getText().toString();
        String amountStr = etOtherIncomeAmount.getText().toString();
        if (!name.isEmpty() && !amountStr.isEmpty()) {
            ExtraIncome otherIncome = new ExtraIncome();
            otherIncome.setDescription(name);
            otherIncome.setAmount(Double.parseDouble(amountStr));

            int selectedId = rgOtherIncomeType.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            otherIncome.setRecurring("Recurring".equals(radioButton.getText().toString()));

            // For recurring, you might want a start date, for one-time, today's date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            otherIncome.setIncomeMonth(sdf.format(Calendar.getInstance().getTime()));

            saveExtraIncome(otherIncome);
        }
    }

    private void saveExtraIncome(ExtraIncome income) {
        apiService.addExtraIncome(income).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, "Failed to save income: " + income.getDescription(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network error while saving income.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.getMenu().findItem(R.id.nav_spend).setChecked(true);
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}


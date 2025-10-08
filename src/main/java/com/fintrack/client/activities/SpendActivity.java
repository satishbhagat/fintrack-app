package com.fintrack.client.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.fintrack.client.R;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.models.AddIncomeRequest;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SpendActivity extends BaseActivity {

    private TextInputEditText etSalary, etBonusAmount, etOtherIncomeName, etOtherIncomeAmount;
    private LinearLayout containerFixedExpenses, containerCreditCards;
    private MaterialButton buttonAddExpense, buttonAddCard;
    private TextView tvBonusDate;
    private SwitchMaterial switchBonusRecurring, switchOtherIncomeRecurring;

    private ApiService apiService;
    private String emailId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spend);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        setToolbarTitle("Manage Finances");

        emailId = UserSession.getInstance().getEmailId();
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Initialize UI components
        etSalary = findViewById(R.id.etSalary);
        containerFixedExpenses = findViewById(R.id.containerFixedExpenses);
        containerCreditCards = findViewById(R.id.containerCreditCards);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonAddCard = findViewById(R.id.buttonAddCard);
        etBonusAmount = findViewById(R.id.etBonusAmount);
        tvBonusDate = findViewById(R.id.tvBonusDate);
        switchBonusRecurring = findViewById(R.id.switchBonusRecurring);
        etOtherIncomeName = findViewById(R.id.etOtherIncomeName);
        etOtherIncomeAmount = findViewById(R.id.etOtherIncomeAmount);
        switchOtherIncomeRecurring = findViewById(R.id.switchOtherIncomeRecurring);


        Double salary = UserSession.getInstance().getSalary();
        if (salary != null) {
            etSalary.setText(String.valueOf(salary));
        }

        buttonAddExpense.setOnClickListener(v -> addFixedExpenseView());
        buttonAddCard.setOnClickListener(v -> addCreditCardView());
        tvBonusDate.setOnClickListener(v -> showDatePicker(tvBonusDate));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.spend_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveProfile();
            saveExtraIncome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker(TextView dateTextView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            dateTextView.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    private void setupSpinnerListener(View parentView, int spinnerId, int editTextId) {
        Spinner spinner = parentView.findViewById(spinnerId);
        EditText editText = parentView.findViewById(editTextId);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Other")) {
                    editText.setVisibility(View.VISIBLE);
                } else {
                    editText.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editText.setVisibility(View.GONE);
            }
        });
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

        TextView tvDueDate = cardView.findViewById(R.id.tvDueDate);
        tvDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                tvDueDate.setText(String.format(Locale.getDefault(), "%02d/%02d", dayOfMonth, month + 1));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        containerCreditCards.addView(cardView);
    }

    private void saveExtraIncome() {
        String bonusAmountStr = etBonusAmount.getText().toString();
        if (!bonusAmountStr.isEmpty()) {
            AddIncomeRequest bonusRequest = new AddIncomeRequest();
            bonusRequest.amount = Double.parseDouble(bonusAmountStr);
            bonusRequest.description = "Bonus";
            bonusRequest.month = tvBonusDate.getText().toString();
            apiService.addExtraIncome(bonusRequest).enqueue(getGenericCallback("Bonus"));
        }

        String otherIncomeName = etOtherIncomeName.getText().toString();
        String otherIncomeAmountStr = etOtherIncomeAmount.getText().toString();
        if (!otherIncomeName.isEmpty() && !otherIncomeAmountStr.isEmpty()) {
            AddIncomeRequest otherIncomeRequest = new AddIncomeRequest();
            otherIncomeRequest.amount = Double.parseDouble(otherIncomeAmountStr);
            otherIncomeRequest.description = otherIncomeName;
            Calendar cal = Calendar.getInstance();
            otherIncomeRequest.month = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
            apiService.addExtraIncome(otherIncomeRequest).enqueue(getGenericCallback("Other Income"));
        }
    }

    private Callback<GenericResponse> getGenericCallback(String incomeType) {
        return new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpendActivity.this, incomeType + " saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SpendActivity.this, "Failed to save " + incomeType, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(SpendActivity.this, "Network error saving " + incomeType, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void saveProfile() {
        String salaryStr = etSalary.getText().toString();
        if (salaryStr.isEmpty()) {
            Toast.makeText(this, "Please enter your monthly salary.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.getMenu().findItem(R.id.nav_spend).setChecked(true);
        }
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}


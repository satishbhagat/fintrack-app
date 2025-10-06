package com.fintrack.client.activities;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.fintrack.client.R;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.dto.ProfileSetupRequest;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.network.ApiService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.fintrack.client.utils.UserSession;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private EditText editTextSalary;
    private LinearLayout containerFixedExpenses, containerCreditCards;
    private Button buttonAddExpense, buttonAddCard, buttonSaveProfile;
    private ApiService apiService;

    private String emailId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the dashboard layout into the BaseActivity's FrameLayout
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        View cardView = getLayoutInflater().inflate(R.layout.activity_profile, contentFrame, true);
        // Find tvDueDate in the inflated cardView


        setToolbarTitle("Expenditures");

        emailId = UserSession.getInstance().getEmailId();

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        editTextSalary = findViewById(R.id.editTextSalary);
        containerFixedExpenses = findViewById(R.id.containerFixedExpenses);
        containerCreditCards = findViewById(R.id.containerCreditCards);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonAddCard = findViewById(R.id.buttonAddCard);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        //set salary
        editTextSalary.setText(UserSession.getInstance().getSalary().toString());

        buttonAddExpense.setOnClickListener(v -> addFixedExpenseView());
        buttonAddCard.setOnClickListener(v -> addCreditCardView());
        buttonSaveProfile.setOnClickListener(v -> saveProfile());

    }

    private void addFixedExpenseView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View expenseView = inflater.inflate(R.layout.item_fixed_expense, containerFixedExpenses, false);
        containerFixedExpenses.addView(expenseView);
    }

    private void addCreditCardView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.item_credit_card, containerCreditCards, false);

        // Find tvDueDate in the inflated cardView
        TextView tvDueDate = cardView.findViewById(R.id.tvDueDate);

        // Set OnClickListener for tvDueDate
        tvDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                tvDueDate.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });

        // Add the cardView to the container
        containerCreditCards.addView(cardView);
    }

    // You will need to create these two simple layout files:
    // item_fixed_expense.xml: A LinearLayout with two EditTexts (name, amount).
    // item_credit_card.xml: A LinearLayout with one EditText (card name).

    private void saveProfile() {
        String salaryStr = editTextSalary.getText().toString();
        if (salaryStr.isEmpty()) {
            Toast.makeText(this, "Please enter your monthly salary.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProfileSetupRequest request = new ProfileSetupRequest();
        request.setEmailId(emailId);
        request.setMonthlySalary(new BigDecimal(salaryStr));
        request.setFixedExpenditures(getFixedExpensesFromViews());
        request.setCreditCards(getCreditCardsFromViews());


        apiService.setupProfile(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile saved!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish(); // Finish this activity so user can't go back
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to save profile. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<ProfileSetupRequest.FixedExpenditureDto> getFixedExpensesFromViews() {
        List<ProfileSetupRequest.FixedExpenditureDto> expenses = new ArrayList<>();
        for (int i = 0; i < containerFixedExpenses.getChildCount(); i++) {
            View view = containerFixedExpenses.getChildAt(i);
            EditText nameEditText = view.findViewById(R.id.spinnerExpenseName);
            EditText amountEditText = view.findViewById(R.id.etExpenseAmount);

            String name = nameEditText.getText().toString();
            String amountStr = amountEditText.getText().toString();

            if (!name.isEmpty() && !amountStr.isEmpty()) {
                ProfileSetupRequest.FixedExpenditureDto dto = new ProfileSetupRequest.FixedExpenditureDto();
                dto.setName(name);
                dto.setAmount(new BigDecimal(amountStr));
                expenses.add(dto);
            }
        }
        return expenses;
    }

    private List<ProfileSetupRequest.CreditCardDto> getCreditCardsFromViews() {
        List<ProfileSetupRequest.CreditCardDto> cardDtos = new ArrayList<>();
        for (int i = 0; i < containerCreditCards.getChildCount(); i++) {
            ProfileSetupRequest.CreditCardDto creditCardDto =new ProfileSetupRequest.CreditCardDto();
            View view = containerCreditCards.getChildAt(i);
            EditText cardNameEditText = view.findViewById(R.id.editTextCardName);
            String name = cardNameEditText.getText().toString();
            if (!name.isEmpty()) {
                creditCardDto.setCardName(name);
                creditCardDto.setUserId(UserSession.getInstance().getUserId());
            }
            cardDtos.add(creditCardDto);
        }
        return cardDtos;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Highlight the "Home" tab when this activity is visible
        bottomNavigationView.getMenu().findItem(R.id.nav_spend).setChecked(true);
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}

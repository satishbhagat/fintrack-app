package com.fintrack.client.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fintrack.client.R;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.dto.ProfileSetupRequest;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.network.ApiService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText editTextSalary;
    private LinearLayout containerFixedExpenses, containerCreditCards;
    private Button buttonAddExpense, buttonAddCard, buttonSaveProfile;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        editTextSalary = findViewById(R.id.editTextSalary);
        containerFixedExpenses = findViewById(R.id.containerFixedExpenses);
        containerCreditCards = findViewById(R.id.containerCreditCards);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonAddCard = findViewById(R.id.buttonAddCard);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);

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
        request.setMonthlySalary(new BigDecimal(salaryStr));
        request.setFixedExpenditures(getFixedExpensesFromViews());
        request.setCreditCardNames(getCreditCardsFromViews());

        String authToken = "Bearer " + getIntent().getStringExtra("AUTH_TOKEN");

        apiService.setupProfile(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile saved!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
                    // Pass the token along to the next activity
                    intent.putExtra("AUTH_TOKEN", getIntent().getStringExtra("AUTH_TOKEN"));
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
            EditText nameEditText = view.findViewById(R.id.editTextExpenseName);
            EditText amountEditText = view.findViewById(R.id.editTextExpenseAmount);

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

    private List<String> getCreditCardsFromViews() {
        List<String> cardNames = new ArrayList<>();
        for (int i = 0; i < containerCreditCards.getChildCount(); i++) {
            View view = containerCreditCards.getChildAt(i);
            EditText cardNameEditText = view.findViewById(R.id.editTextCardName);
            String name = cardNameEditText.getText().toString();
            if (!name.isEmpty()) {
                cardNames.add(name);
            }
        }
        return cardNames;
    }
}

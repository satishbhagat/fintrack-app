package com.fintrack.client.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.fintrack.client.R;
import com.fintrack.client.models.SavingsGoal;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GoalsActivity extends BaseActivity {

    private static final String TAG = "GoalsActivity";

    private TextInputEditText etGoalName, etTargetAmount, etTargetDate;
    private Button btnCalculateGoal;
    private TextView tvMonthlyContribution;
    private LinearLayout containerExistingGoals; // To display saved goals

    private ApiService apiService;
    private Calendar selectedTargetDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setToolbarTitle("Savings Goals");

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        etGoalName = findViewById(R.id.etGoalName);
        etTargetAmount = findViewById(R.id.etTargetAmount);
        etTargetDate = findViewById(R.id.etTargetDate);
        btnCalculateGoal = findViewById(R.id.btnCalculateGoal);
        tvMonthlyContribution = findViewById(R.id.tvMonthlyContribution);
        containerExistingGoals = findViewById(R.id.containerExistingGoals);

        setupDatePicker();

        btnCalculateGoal.setOnClickListener(v -> calculateAndSaveGoal());

        // Load existing goals when the activity starts
        fetchExistingGoals();
    }

    private void setupDatePicker() {
        etTargetDate.setOnClickListener(v -> {
            Calendar currentDate = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(GoalsActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        selectedTargetDate.set(Calendar.YEAR, year);
                        selectedTargetDate.set(Calendar.MONTH, monthOfYear);
                        selectedTargetDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etTargetDate.setText(dateFormat.format(selectedTargetDate.getTime()));
                    },
                    selectedTargetDate.get(Calendar.YEAR),
                    selectedTargetDate.get(Calendar.MONTH),
                    selectedTargetDate.get(Calendar.DAY_OF_MONTH));

            // Set the minimum date to tomorrow to avoid selecting past dates
            datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis() + TimeUnit.DAYS.toMillis(1));
            datePickerDialog.show();
        });
    }

    private void calculateAndSaveGoal() {
        String goalName = etGoalName.getText().toString().trim();
        String amountStr = etTargetAmount.getText().toString().trim();
        String dateStr = etTargetDate.getText().toString().trim();

        if (goalName.isEmpty() || amountStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Please fill all goal details.", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal targetAmount;
        Date targetDate;
        try {
            targetAmount = new BigDecimal(amountStr);
            targetDate = dateFormat.parse(dateStr);
            if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(this, "Target amount must be positive.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid target amount.", Toast.LENGTH_SHORT).show();
            return;
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format.", Toast.LENGTH_SHORT).show();
            return; // Should not happen with DatePickerDialog, but good practice
        }

        // Calculate months difference
        Calendar startCal = Calendar.getInstance(); // Today
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(targetDate);

        // Ensure target date is in the future
        if (!endCal.after(startCal)) {
            Toast.makeText(this, "Target date must be in the future.", Toast.LENGTH_SHORT).show();
            return;
        }

        int diffYear = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH);

        // Include the current month if the target date is later this month, otherwise start from next month
        if (endCal.get(Calendar.DAY_OF_MONTH) > startCal.get(Calendar.DAY_OF_MONTH) && diffMonth == 0){
            diffMonth = 1; // Need at least one month contribution
        } else if (diffMonth <= 0) {
            // If target date is next month but very early, still count as 1 month. Adjust if needed.
            // For simplicity, let's ensure at least 1 month difference if date is future.
            diffMonth = Math.max(diffMonth, 1);
        }


        if (diffMonth <= 0) {
            // Recalculate if something went wrong, ensure at least 1 month
            long diffMillis = targetDate.getTime() - startCal.getTimeInMillis();
            long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
            diffMonth = (int) Math.max(1, (diffDays / 30)); // Approximate if error, ensure at least 1
            Log.w(TAG, "Month difference calculation was zero or negative, approximated to: " + diffMonth);
            //Toast.makeText(this, "Target date must be at least one month in the future.", Toast.LENGTH_SHORT).show();
            // return;
        }

        BigDecimal monthlyContribution = targetAmount.divide(new BigDecimal(diffMonth), 2, RoundingMode.CEILING);
        tvMonthlyContribution.setText(String.format(Locale.getDefault(), "Required Monthly Contribution: ₹%.2f", monthlyContribution));
        tvMonthlyContribution.setVisibility(View.VISIBLE);

        // --- Save Goal (Backend Integration Needed) ---
        UUID userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            Toast.makeText(this, "User session error. Cannot save goal.", Toast.LENGTH_LONG).show();
            return;
        }

        SavingsGoal newGoal = new SavingsGoal();
        newGoal.setUserId(userId);
        newGoal.setGoalName(goalName);
        newGoal.setTargetAmount(targetAmount);
        newGoal.setTargetDate(dateStr); // Send date as string
        newGoal.setMonthlyContribution(monthlyContribution);

        // Call API to save the goal
        apiService.saveSavingsGoal(newGoal).enqueue(new Callback<SavingsGoal>() { // Expecting SavingsGoal back
            @Override
            public void onResponse(Call<SavingsGoal> call, Response<SavingsGoal> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(GoalsActivity.this, "Goal saved successfully!", Toast.LENGTH_SHORT).show();
                    // Clear input fields
                    etGoalName.setText("");
                    etTargetAmount.setText("");
                    etTargetDate.setText("");
                    tvMonthlyContribution.setText("");
                    tvMonthlyContribution.setVisibility(View.GONE);
                    // Refresh the list of existing goals
                    fetchExistingGoals();
                } else {
                    String errorMsg = "Failed to save goal.";
                    try { errorMsg += " Error: " + response.errorBody().string(); } catch (Exception e) {}
                    Toast.makeText(GoalsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to save goal: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<SavingsGoal> call, Throwable t) {
                Toast.makeText(GoalsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network error saving goal", t);
            }
        });
        // ---------------------------------------------
    }

    private void fetchExistingGoals() {
        UUID userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            Log.e(TAG, "User ID is null, cannot fetch goals.");
            // Optionally show a message or handle login state
            return;
        }

        apiService.getSavingsGoals(userId).enqueue(new Callback<List<SavingsGoal>>() {
            @Override
            public void onResponse(Call<List<SavingsGoal>> call, Response<List<SavingsGoal>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayExistingGoals(response.body());
                } else {
                    String errorMsg = "Failed to fetch goals.";
                    try { errorMsg += " Error: " + response.errorBody().string(); } catch (Exception e) {}
                    Log.e(TAG, "Failed to fetch goals: " + errorMsg);
                    Toast.makeText(GoalsActivity.this, "Could not load existing goals.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SavingsGoal>> call, Throwable t) {
                Log.e(TAG, "Network error fetching goals", t);
                Toast.makeText(GoalsActivity.this, "Network error fetching goals.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayExistingGoals(List<SavingsGoal> goals) {
        containerExistingGoals.removeAllViews(); // Clear previous views
        if (goals == null || goals.isEmpty()) {
            TextView noGoalsText = new TextView(this);
            noGoalsText.setText("No savings goals set yet.");
            noGoalsText.setPadding(0, 16, 0, 0);
            containerExistingGoals.addView(noGoalsText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (SavingsGoal goal : goals) {
            // You should create a dedicated layout file for a goal item (e.g., list_item_goal.xml)
            // For simplicity, using a basic TextView here. Inflate your custom layout instead.
            TextView goalView = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, containerExistingGoals, false);

            String goalText = String.format(Locale.getDefault(),
                    "%s - ₹%.2f by %s (Save ₹%.2f/month)",
                    goal.getGoalName(),
                    goal.getTargetAmount(),
                    goal.getTargetDate(),
                    goal.getMonthlyContribution());
            goalView.setText(goalText);
            goalView.setPadding(0, 8, 0, 8); // Add some padding

            // Add edit/delete functionality here if needed (e.g., setOnClickListener)

            containerExistingGoals.addView(goalView);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.getMenu().findItem(R.id.nav_goals).setChecked(true); // Assuming R.id.nav_goals exists
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}

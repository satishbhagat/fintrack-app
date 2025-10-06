// DashboardActivity.java
package com.fintrack.client.activities;


import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.fintrack.client.R;
import com.fintrack.client.adapters.ExpenseAdapter;
import com.fintrack.client.dto.DashboardRequest;
import com.fintrack.client.dto.DashboardResponse;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.network.ApiService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fintrack.client.utils.UserSession;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.fintrack.client.R.*;

public class DashboardActivity extends BaseActivity {

    private static final String TAG = "DashboardActivity";

    private ExpenseAdapter expensesAdapter;
    private ApiService apiService;

    private TableLayout tableLayout;

    private TextView tvAmountNeeded;

    private TextView tvTotalAmount;

    private TextView tvSavings;

    private Button btnSaveDashboard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the dashboard layout into the BaseActivity's FrameLayout
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_dashboard, contentFrame, true);

        setToolbarTitle("Dashboard");

        String emailId = UserSession.getInstance().getEmailId();
        Log.d(TAG, "Received emailId: " + emailId);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Bind UI components
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvSavings = findViewById(R.id.tvSavings);
        tvAmountNeeded = findViewById(R.id.tvAmountNeeded);
        tableLayout = findViewById(R.id.tableLayout);
        btnSaveDashboard = findViewById(R.id.btnSaveDashboard);

        // Update the table dynamically
        fetchDashboardData(emailId, tvTotalAmount, tvSavings, tvAmountNeeded);

        btnSaveDashboard.setOnClickListener(v -> saveExpenses());
    }

    private void saveExpenses() {
        List<DashboardRequest.MonthlyExpenseItem> expenses = new ArrayList<>();

        // Iterate through table rows (skip the header row)
        int childCount = tableLayout.getChildCount();
        for (int i = 1; i < childCount; i++) { // Start from 1 to skip the header
            TableRow row = (TableRow) tableLayout.getChildAt(i);

            // Extract data from each column
            TextView tvName = (TextView) row.getChildAt(1); // Name column
            TextView tvAmount = (TextView) row.getChildAt(2); // Amount column
            Spinner spinnerStatus = (Spinner) row.getChildAt(3); // Status column

            String name = tvName.getText().toString();
            BigDecimal amount = new BigDecimal(tvAmount.getText().toString().replace("₹", "").trim());
            String status = spinnerStatus.getSelectedItem().toString();

            // Create and add an expense object
            DashboardRequest.MonthlyExpenseItem expense = new DashboardRequest.MonthlyExpenseItem();
            expense.setName(name);
            expense.setAmount(amount);
            expense.setStatus(status);
            expenses.add(expense);
        }

        // Create DashboardRequest and set expenses
        DashboardRequest request = new DashboardRequest();
        request.setExpenses(expenses);
        request.setEmailId(getIntent().getStringExtra("USER_EMAIL")); // Assuming email is used as userId

        // Send the request to the backend
        Call<DashboardResponse> call = apiService.saveDashboard(request);
        call.enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, "Expenses saved successfully!", Toast.LENGTH_SHORT).show();
                    DashboardResponse data = response.body();
                    updateUI(data);
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to save expenses.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "An error occurred: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDashboardData(String emailId, TextView tvTotalAmount, TextView tvSavings, TextView tvAmountNeeded) {
        Log.d(TAG, "Fetching dashboard data for emailId: " + emailId);

        Call<DashboardResponse> call = apiService.getDashboard(emailId);
        call.enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse data = response.body();
                    updateUI(data);
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to load dashboard data.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "An error occurred: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(DashboardResponse data) {
        // Clear existing expense rows (except header)
        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1);
        }

        // Add expense rows dynamically
        List<DashboardResponse.MonthlyExpenseItem> expenses = data.getExpenses();
        for (int i = 0; i < expenses.size(); i++) {
            DashboardResponse.MonthlyExpenseItem expense = expenses.get(i);
            String amountStr = String.format(Locale.getDefault(), "₹%.2f", expense.getAmount());
            String statusStr = expense.getStatus(); // e.g., "Pending" or "Paid"
            addExpenseRow(i + 1, expense.getName(), amountStr, statusStr);
        }

        // Update total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (DashboardResponse.MonthlyExpenseItem exp : expenses) {
            totalAmount = totalAmount.add(exp.getAmount());
        }

        tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));

        // Update savings and amount needed
        BigDecimal monthlySalary = data.getMonthlySalry();
        BigDecimal savings = monthlySalary.subtract(totalAmount);
        tvSavings.setText(String.format(Locale.getDefault(), "Savings: ₹%.2f", savings));

        BigDecimal pendingAmount = BigDecimal.ZERO;
        for (DashboardResponse.MonthlyExpenseItem exp : expenses) {
            if ("Pending".equalsIgnoreCase(exp.getStatus())) {
                pendingAmount = pendingAmount.add(exp.getAmount());
            }
        }
        tvAmountNeeded.setText(String.format(Locale.getDefault(), "Amount Needed: ₹%.2f", pendingAmount));
    }

    private void addExpenseRow(int index, String name, String amount, String status) {
        TableRow row = new TableRow(this);

        TextView tvIndex = new TextView(this);
        tvIndex.setText(String.valueOf(index));
        tvIndex.setPadding(8, 8, 8, 8);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setPadding(8, 8, 8, 8);

        TextView tvAmount = new TextView(this);
        tvAmount.setText(amount);
        tvAmount.setPadding(8, 8, 8, 8);

        Spinner spinnerStatus = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Pending", "Paid"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
        spinnerStatus.setPadding(8, 8, 8, 8);

        // Set the current status
        if ("Paid".equalsIgnoreCase(status)) {
            spinnerStatus.setSelection(1);
        } else {
            spinnerStatus.setSelection(0);
        }

        row.addView(tvIndex);
        row.addView(tvName);
        row.addView(tvAmount);
        row.addView(spinnerStatus);

        tableLayout.addView(row);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Highlight the "Home" tab when this activity is visible
        bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}

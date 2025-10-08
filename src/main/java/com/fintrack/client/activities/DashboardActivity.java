package com.fintrack.client.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fintrack.client.R;
import com.fintrack.client.adapters.ExpenseAdapter;
import com.fintrack.client.dto.DashboardResponse;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends BaseActivity {

    private static final String TAG = "DashboardActivity";

    private ApiService apiService;
    private ExpenseAdapter expenseAdapter;

    private TextView tvTotalBalance, tvSavings, tvAmountNeeded;
    private RecyclerView rvExpenses;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the dashboard layout into the BaseActivity's FrameLayout
        // This is handled by the new layout structure, but we need to set the specific content view
        setContentView(R.layout.activity_dashboard);

        // Re-initialize the toolbar and bottom navigation from the new layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);


        setToolbarTitle("Dashboard");

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Bind UI components
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvSavings = findViewById(R.id.tvSavings);
        tvAmountNeeded = findViewById(R.id.tvAmountNeeded);
        rvExpenses = findViewById(R.id.rvExpenses);
        pieChart = findViewById(R.id.pieChart);

        setupRecyclerView();

        String emailId = UserSession.getInstance().getEmailId();
        if (emailId != null) {
            fetchDashboardData(emailId);
        } else {
            Toast.makeText(this, "User session not found.", Toast.LENGTH_LONG).show();
            // Optionally, redirect to login
        }
    }

    private void setupRecyclerView() {
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        // Pass an empty list to the adapter initially
        expenseAdapter = new ExpenseAdapter(new ArrayList<>());
        rvExpenses.setAdapter(expenseAdapter);
    }

    private void fetchDashboardData(String emailId) {
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
        // Assume DashboardResponse now includes totalIncome from the backend
        // If not, you would need another API call to fetch extra incomes and sum them up
        BigDecimal totalIncome = data.getTotalIncome() != null ? data.getTotalIncome() : data.getMonthlySalry();
        tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", totalIncome));

        BigDecimal totalExpenses = data.getTotalExpenses() != null ? data.getTotalExpenses() : BigDecimal.ZERO;
        // Correct savings calculation
        BigDecimal savings = totalIncome.subtract(totalExpenses);
        tvSavings.setText(String.format(Locale.getDefault(), "₹%.2f", savings));

        BigDecimal pendingAmount = data.getPendingAmount() != null ? data.getPendingAmount() : BigDecimal.ZERO;
        tvAmountNeeded.setText(String.format(Locale.getDefault(), "₹%.2f", pendingAmount));

        if (data.getExpenses() != null) {
            expenseAdapter.updateExpenses(data.getExpenses());
        }

        setupPieChart(data.getExpenses());
    }


    private void setupPieChart(List<DashboardResponse.MonthlyExpenseItem> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (DashboardResponse.MonthlyExpenseItem expense : expenses) {
            entries.add(new PieEntry(expense.getAmount().floatValue(), expense.getName()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Breakdown");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses");
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the correct navigation item is selected
        if (bottomNavigationView != null) {
            bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        }
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}
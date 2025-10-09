package com.fintrack.client.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fintrack.client.R;
import com.fintrack.client.adapters.ExpenseAdapter;
import com.fintrack.client.dto.DashboardResponse;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.models.AddMonthlyExpenseRequest;
import com.fintrack.client.models.MonthlyExpense;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardActivity extends BaseActivity {

    private static final String TAG = "DashboardActivity";

    private ApiService apiService;
    private ExpenseAdapter expenseAdapter;

    private TextView tvTotalBalance, tvSavings, tvAmountNeeded;
    private RecyclerView rvExpenses;
    private PieChart pieChart;
    private FloatingActionButton fabAddExpense;
    private String emailId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

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
        fabAddExpense = findViewById(R.id.fabAddExpense);

        setupRecyclerView();

        fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        emailId = UserSession.getInstance().getEmailId();
        if (emailId != null) {
            fetchDashboardData(emailId);
        } else {
            Toast.makeText(this, "User session not found.", Toast.LENGTH_LONG).show();
            // Optionally, redirect to login
        }
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);
        builder.setTitle("Add New Expense");

        final EditText etExpenseName = dialogView.findViewById(R.id.etExpenseName);
        final EditText etExpenseAmount = dialogView.findViewById(R.id.etExpenseAmount);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etExpenseName.getText().toString().trim();
            String amountStr = etExpenseAmount.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr)) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            AddMonthlyExpenseRequest request = new AddMonthlyExpenseRequest();
           request.setName(name);
           request.setUserId(UserSession.getInstance().getUserId());
           request.setAmount(Double.parseDouble(amountStr));
           request.setStatus("PENDING");
            // Set month to today's date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            request.month = sdf.format(Calendar.getInstance().getTime());

            apiService.addMonthlyExpense(request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(DashboardActivity.this, "Expense added successfully!", Toast.LENGTH_SHORT).show();

                        // Optimistic UI Update for instant feedback
                        DashboardResponse.MonthlyExpenseItem newItem = new DashboardResponse.MonthlyExpenseItem();
                        newItem.setName(name);
                        newItem.setAmount(new BigDecimal(amountStr));
                        newItem.setStatus("PENDING");

                        newItem.setId(UUID.randomUUID().toString()); // Placeholder ID

                        expenseAdapter.addExpense(newItem);
                        rvExpenses.scrollToPosition(0); // Scroll to the new item at the top

                        // Convert adapter's list and update pie chart immediately
                        List<MonthlyExpense> optimisticList = expenseAdapter.getCurrentExpenses();
                        List<DashboardResponse.MonthlyExpenseItem> chartList = new ArrayList<>();
                        for (MonthlyExpense expense : optimisticList) {
                            DashboardResponse.MonthlyExpenseItem item = new DashboardResponse.MonthlyExpenseItem();
                            item.setName(expense.name);
                            item.setAmount(BigDecimal.valueOf(expense.amount));
                            chartList.add(item);
                        }
                        setupPieChart(chartList);

                        // Fetch authoritative data from server to update totals and get real ID
                        fetchDashboardData(emailId);
                    } else {
                        Toast.makeText(DashboardActivity.this, "Failed to add expense.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
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
        BigDecimal totalIncome = data.getTotalIncome() != null ? data.getTotalIncome() : BigDecimal.ZERO;
        tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", totalIncome));

        BigDecimal totalExpenses = data.getTotalExpenses() != null ? data.getTotalExpenses() : BigDecimal.ZERO;
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


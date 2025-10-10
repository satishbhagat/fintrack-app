package com.fintrack.client.activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fintrack.client.R;
import com.fintrack.client.adapters.ExpenseAdapter;
import com.fintrack.client.dto.DashboardResponse;
import com.fintrack.client.models.AbstractExpenseItem;
import com.fintrack.client.models.AddMonthlyExpenseRequest;
import com.fintrack.client.models.MonthlyExpense;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends BaseActivity implements ExpenseAdapter.OnExpenseStatusChangedListener {

    private static final String TAG = "DashboardActivity";

    private ApiService apiService;
    private ExpenseAdapter expenseAdapter;
    private String userEmail;

    private TextView tvTotalIncome, tvSavings, tvAmountNeeded;
    private RecyclerView rvExpenses;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setToolbarTitle("Dashboard");

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        apiService = RetrofitClient.getInstance().create(ApiService.class);
        userEmail = UserSession.getInstance().getEmailId();

        tvTotalIncome = findViewById(R.id.tvTotalBalance);
        tvSavings = findViewById(R.id.tvSavings);
        tvAmountNeeded = findViewById(R.id.tvAmountNeeded);
        rvExpenses = findViewById(R.id.rvExpenses);
        pieChart = findViewById(R.id.pieChart);
        ImageButton btnAddExpense = findViewById(R.id.btnAddExpense);

        setupRecyclerView();
        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        if (userEmail != null) {
            fetchDashboardData(userEmail);
        } else {
            Toast.makeText(this, "User session not found.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerView() {
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), this); // Pass 'this' as the listener
        rvExpenses.setAdapter(expenseAdapter);
    }

    private void fetchDashboardData(String emailId) {
        apiService.getDashboard(emailId).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
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
        tvTotalIncome.setText(String.format(Locale.getDefault(), "₹%.2f", data.getTotalIncome()));
        expenseAdapter.updateExpenses(data.getExpenses(), data.getFixedExpenditures());
        updateSummaryMetrics(); // Use the new method to calculate and set totals
        setupPieChart(expenseAdapter.getCurrentExpenses());
    }

    private void updateSummaryMetrics() {
        List<AbstractExpenseItem> currentExpenses = expenseAdapter.getCurrentExpenses();
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;

        for (AbstractExpenseItem expense : currentExpenses) {
            totalExpenses = totalExpenses.add(expense.getAmount());
            if ("PENDING".equalsIgnoreCase(expense.getStatus())) {
                pendingAmount = pendingAmount.add(expense.getAmount());
            }
        }

        BigDecimal totalIncome = new BigDecimal(tvTotalIncome.getText().toString().replace("₹", "").trim());
        BigDecimal savings = totalIncome.subtract(totalExpenses);

        tvSavings.setText(String.format(Locale.getDefault(), "₹%.2f", savings));
        tvAmountNeeded.setText(String.format(Locale.getDefault(), "₹%.2f", pendingAmount));
    }


    private void setupPieChart(List<AbstractExpenseItem> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (AbstractExpenseItem expense : expenses) {
            entries.add(new PieEntry(expense.getAmount().floatValue(), expense.getName()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Breakdown");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true);
        pieChart.setCenterText("Expenses");
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        final EditText etExpenseName = dialogView.findViewById(R.id.etExpenseName);
        final EditText etExpenseAmount = dialogView.findViewById(R.id.etExpenseAmount);
        final Button btnSave = dialogView.findViewById(R.id.btnSaveExpense);
        final Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etExpenseName.getText().toString().trim();
            String amountStr = etExpenseAmount.getText().toString().trim();

            if (name.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal amount = new BigDecimal(amountStr);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = sdf.format(new Date());

            AddMonthlyExpenseRequest request = new AddMonthlyExpenseRequest();
            request.name = name;
            request.amount = amount.doubleValue();
            request.month = currentDate;
            request.setUserId(UserSession.getInstance().getUserId());
            apiService.addMonthlyExpense(request).enqueue(new Callback<MonthlyExpense>() {
                @Override
                public void onResponse(Call<MonthlyExpense> call, Response<MonthlyExpense> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        expenseAdapter.addExpense(response.body());
                        rvExpenses.scrollToPosition(0);
                        updateSummaryMetrics();
                        setupPieChart(expenseAdapter.getCurrentExpenses());
                    } else {
                        Toast.makeText(DashboardActivity.this, "Failed to add expense.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MonthlyExpense> call, Throwable t) {
                    Toast.makeText(DashboardActivity.this, "Network error on add.", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        }
    }

    @Override
    public void onStatusChanged() {
        // This method is called from the adapter when a switch is toggled
        updateSummaryMetrics();
    }
}


package com.fintrack.client.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.fintrack.client.models.AbstractExpenseItem;
import com.fintrack.client.models.AddMonthlyExpenseRequest;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
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
import java.util.*;

public class DashboardActivity extends BaseActivity {

    private static final String TAG = "DashboardActivity";

    private ApiService apiService;
    private ExpenseAdapter expenseAdapter;

    private TextView tvTotalBalance, tvSavings, tvAmountNeeded;
    private RecyclerView rvExpenses;
    private PieChart pieChart;
    private ImageButton btnAddExpense;
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
        btnAddExpense = findViewById(R.id.btnAddExpense);

        setupRecyclerView();

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());

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
            request.name = name;
            request.amount = Double.parseDouble(amountStr);
            request.status = "PENDING";
            // Set month to today's date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            request.month = sdf.format(Calendar.getInstance().getTime());

            apiService.addMonthlyExpense(request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(DashboardActivity.this, "Expense added successfully!", Toast.LENGTH_SHORT).show();

                        DashboardResponse.MonthlyExpenseItem newItem = new DashboardResponse.MonthlyExpenseItem();
                        newItem.setName(name);
                        newItem.setAmount(new BigDecimal(amountStr));
                        newItem.setStatus("PENDING");
                        newItem.setId(UUID.randomUUID().toString()); // Placeholder ID

                        expenseAdapter.addExpense(newItem);
                        rvExpenses.scrollToPosition(0);

                        List<AbstractExpenseItem> optimisticList = expenseAdapter.getCurrentExpenses();
                        List<DashboardResponse.MonthlyExpenseItem> chartList = new ArrayList<>();
                        for (AbstractExpenseItem expense : optimisticList) {
                            DashboardResponse.MonthlyExpenseItem item = new DashboardResponse.MonthlyExpenseItem();
                            item.setName(expense.getName());
                            item.setAmount(expense.getAmount());
                            chartList.add(item);
                        }
                        setupPieChart(chartList);

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

        expenseAdapter.updateExpenses(data.getExpenses(), data.getFixedExpenditures());

        List<DashboardResponse.MonthlyExpenseItem> combinedListForChart = new ArrayList<>();
        if (data.getExpenses() != null) {
            combinedListForChart.addAll(data.getExpenses());
        }
        if (data.getFixedExpenditures() != null) {
            for(DashboardResponse.FixedExpenditureItem fixedItem : data.getFixedExpenditures()) {
                DashboardResponse.MonthlyExpenseItem chartItem = new DashboardResponse.MonthlyExpenseItem();
                chartItem.setName(fixedItem.getName());
                chartItem.setAmount(fixedItem.getAmount());
                combinedListForChart.add(chartItem);
            }
        }
        setupPieChart(combinedListForChart);
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

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.5f);
        dataSet.setValueLinePart2Length(0.2f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);


        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(16f);


        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(10f);
        pieData.setValueTextColor(Color.BLACK);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(pieData);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
        legend.setWordWrapEnabled(true);


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


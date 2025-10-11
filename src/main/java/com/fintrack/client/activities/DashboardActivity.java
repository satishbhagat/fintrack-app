package com.fintrack.client.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
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
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends BaseActivity implements ExpenseAdapter.OnExpenseStatusChangedListener {

    private static final String TAG = "DashboardActivity";

    private ApiService apiService;
    private ExpenseAdapter expenseAdapter;

    private TextView tvTotalIncome, tvSavings, tvAmountNeeded, tvSelectedMonth, toolbarTitle;
    private RecyclerView rvExpenses;
    private PieChart pieChart;
    private ImageButton btnAddExpense;
    private LinearLayout monthSelectorContainer;

    private int selectedYear;
    private int selectedMonth; // 1-12
    private BigDecimal currentTotalIncome = BigDecimal.ZERO; // State variable for total income

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        toolbarTitle = findViewById(R.id.toolbar_title);
        setToolbarTitle("Dashboard");

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Initialize views
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvSavings = findViewById(R.id.tvSavings);
        tvAmountNeeded = findViewById(R.id.tvAmountNeeded);
        rvExpenses = findViewById(R.id.rvExpenses);
        pieChart = findViewById(R.id.pieChart);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        monthSelectorContainer = findViewById(R.id.month_selector_container);
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);

        setupRecyclerView();

        // Set initial month to current month
        Calendar cal = Calendar.getInstance();
        selectedYear = cal.get(Calendar.YEAR);
        selectedMonth = cal.get(Calendar.MONTH) + 1;

        updateMonthSelectorText();
        fetchDashboardData();

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
        monthSelectorContainer.setOnClickListener(v -> showMonthYearPickerDialog());
    }

    private void showMonthYearPickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_month_year_picker);

        final NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);
        Button btnSelect = dialog.findViewById(R.id.btnSelectMonth);

        // Month Picker
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(new DateFormatSymbols().getMonths());
        monthPicker.setValue(selectedMonth);

        // Year Picker
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 5);
        yearPicker.setMaxValue(currentYear + 5);
        yearPicker.setValue(selectedYear);

        btnSelect.setOnClickListener(v -> {
            selectedYear = yearPicker.getValue();
            selectedMonth = monthPicker.getValue();
            updateMonthSelectorText();
            fetchDashboardData();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateMonthSelectorText() {
        String monthName = new DateFormatSymbols().getMonths()[selectedMonth - 1];
        tvSelectedMonth.setText(String.format(Locale.getDefault(), "%s %d", monthName, selectedYear));
    }

    private void fetchDashboardData() {
        String emailId = UserSession.getInstance().getEmailId();
        if (emailId == null) {
            Toast.makeText(this, "User session not found. Please log in again.", Toast.LENGTH_LONG).show();
            // Here you might want to redirect to LoginActivity
            return;
        }

        Log.d(TAG, "Fetching dashboard data for " + selectedMonth + "/" + selectedYear);
        apiService.getDashboard(emailId, selectedYear, selectedMonth).enqueue(new Callback<DashboardResponse>() {
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
                Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(DashboardResponse data) {
        // Store total income in the state variable and update the UI
        currentTotalIncome = data.getTotalIncome() != null ? data.getTotalIncome() : BigDecimal.ZERO;
        tvTotalIncome.setText(String.format(Locale.getDefault(), "₹%.2f", currentTotalIncome));

        // Update adapter with both expense lists
        expenseAdapter.updateExpenses(data.getExpenses(), data.getFixedExpenditures());

        // Recalculate and update summary based on the new list
        recalculateSummary();

        // Check if the selected month is in the past to set read-only mode
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        boolean isPastMonth = selectedYear < currentYear || (selectedYear == currentYear && selectedMonth < currentMonth);
        setReadOnlyMode(isPastMonth);
    }

    private void setReadOnlyMode(boolean isReadOnly) {
        btnAddExpense.setVisibility(isReadOnly ? View.GONE : View.VISIBLE);
        expenseAdapter.setReadOnly(isReadOnly);
    }

    private void setupRecyclerView() {
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), this);
        rvExpenses.setAdapter(expenseAdapter);
    }

    @Override
    public void onStatusChanged() {
        // When status changes, recalculate the summary using the stored total income
        recalculateSummary();
    }

    private void recalculateSummary() {
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal amountNeeded = BigDecimal.ZERO;

        for (AbstractExpenseItem item : expenseAdapter.getCurrentExpenses()) {
            totalExpenses = totalExpenses.add(item.getAmount());
            if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                amountNeeded = amountNeeded.add(item.getAmount());
            }
        }

        BigDecimal savings = currentTotalIncome.subtract(totalExpenses);
        tvSavings.setText(String.format(Locale.getDefault(), "₹%.2f", savings));
        tvAmountNeeded.setText(String.format(Locale.getDefault(), "₹%.2f", amountNeeded));

        // Refresh pie chart with new data
        setupPieChart(expenseAdapter.getCurrentExpenses());
    }

    private void setupPieChart(List<AbstractExpenseItem> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (AbstractExpenseItem expense : expenses) {
            // Only add entries with a positive amount to the chart
            if (expense.getAmount().floatValue() > 0) {
                entries.add(new PieEntry(expense.getAmount().floatValue(), expense.getName()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(16f);
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

        final AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etExpenseName.getText().toString().trim();
            String amountStr = etExpenseAmount.getText().toString().trim();

            if (name.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            AddMonthlyExpenseRequest request = new AddMonthlyExpenseRequest();
            request.name = name;
            request.amount = Double.parseDouble(amountStr);
            // Use the selected month and year for the new expense
            request.month = String.format(Locale.ROOT, "%04d-%02d-01", selectedYear, selectedMonth);

            apiService.addMonthlyExpense(request).enqueue(new Callback<MonthlyExpense>() {
                @Override
                public void onResponse(Call<MonthlyExpense> call, Response<MonthlyExpense> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(DashboardActivity.this, "Expense added!", Toast.LENGTH_SHORT).show();
                        // Optimistic UI update
                        expenseAdapter.addExpense(response.body());
                        recalculateSummary();
                    } else {
                        Toast.makeText(DashboardActivity.this, "Failed to add expense.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MonthlyExpense> call, Throwable t) {
                    Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
        if (toolbarTitle != null) {
            toolbarTitle.setText(title);
        }
    }
}


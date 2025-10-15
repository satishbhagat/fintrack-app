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
import com.fintrack.client.models.UpdateExpenseRequest;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.ExpenseSorter;
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
import java.util.*;

public class DashboardActivity extends BaseActivity implements ExpenseAdapter.OnExpenseInteractionListener {

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

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog(null));
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

        // Update expense list title
        TextView tvExpenseListTitle = findViewById(R.id.tvExpenseListTitle);
        tvExpenseListTitle.setText(String.format("%s Expenses", monthName));
    }

    private void fetchDashboardData() {
        String emailId = UserSession.getInstance().getEmailId();
        if (emailId == null) {
            Toast.makeText(this, "User session not found. Please log in again.", Toast.LENGTH_LONG).show();
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
        currentTotalIncome = data.getTotalIncome() != null ? data.getTotalIncome() : BigDecimal.ZERO;
        tvTotalIncome.setText(String.format(Locale.getDefault(), "₹%.2f", currentTotalIncome));

        List<AbstractExpenseItem> combinedList = new ArrayList<>();
        if (data.getFixedExpenditures() != null) {
            combinedList.addAll(data.getFixedExpenditures());
        }
        if (data.getExpenses() != null) {
            combinedList.addAll(data.getExpenses());
        }
        // Sort combined list by amount descending
        ExpenseSorter.sortByAmountDescending(combinedList);
        // Handle credit cards: Merge them as placeholders if not already in monthly expenses
        if (data.getCreditCards() != null) {
            for (DashboardResponse.CreditCardItem card : data.getCreditCards()) {
                boolean alreadyExists = false;
                for (AbstractExpenseItem item : combinedList) {
                    if (card.getCardName().equalsIgnoreCase(item.getName())) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    DashboardResponse.MonthlyExpenseItem placeholder = new DashboardResponse.MonthlyExpenseItem();
                    placeholder.setName(card.getCardName());
                    placeholder.setAmount(BigDecimal.ZERO);
                    placeholder.setStatus("PENDING");
                    combinedList.add(placeholder);
                }
            }
        }

        expenseAdapter.updateExpenses(combinedList);
        recalculateSummary();

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
        recalculateSummary();
    }

    @Override
    public void onAmountClicked(AbstractExpenseItem item) {
        showAddExpenseDialog(item);
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

        setupPieChart(expenseAdapter.getCurrentExpenses());
    }

    private void setupPieChart(List<AbstractExpenseItem> expenses) {
        // Initially collapse the pie chart
        pieChart.setVisibility(View.GONE);
        View chartContainer = findViewById(R.id.chart_container);
        TextView chartToggle = findViewById(R.id.chart_toggle);

        chartToggle.setOnClickListener(v -> {
            boolean isVisible = pieChart.getVisibility() == View.VISIBLE;
            pieChart.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            chartToggle.setText(isVisible ? "Show Chart" : "Hide Chart");
        });

        if (expenses == null || expenses.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (AbstractExpenseItem expense : expenses) {
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

    private void showAddExpenseDialog(final AbstractExpenseItem itemToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        final EditText etExpenseName = dialogView.findViewById(R.id.etExpenseName);
        final EditText etExpenseAmount = dialogView.findViewById(R.id.etExpenseAmount);
        final Button btnSave = dialogView.findViewById(R.id.btnSaveExpense);
        final Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        if (itemToEdit != null) {
            etExpenseName.setText(itemToEdit.getName());
            etExpenseAmount.setText(itemToEdit.getAmount().toPlainString());
            // If ID is null, it's a placeholder credit card bill. Lock the name.
            if (itemToEdit.getId() == null) {
                etExpenseName.setEnabled(false);
            }
        }

        final AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etExpenseName.getText().toString().trim();
            String amountStr = etExpenseAmount.getText().toString().trim();

            if (name.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            // If itemToEdit is null (new from FAB) OR its ID is null (placeholder credit card), create a new expense.
            if (itemToEdit == null || itemToEdit.getId() == null) {
                AddMonthlyExpenseRequest request = new AddMonthlyExpenseRequest();
                request.name = name;
                request.userId = UserSession.getInstance().getUserId();
                request.amount = amount;
                request.month = String.format(Locale.ROOT, "%04d-%02d-01", selectedYear, selectedMonth);
                saveNewMonthlyExpense(request);
            } else { // Otherwise, update the existing expense
                UpdateExpenseRequest request = new UpdateExpenseRequest();
                request.amount = amount;
                updateExistingExpense(itemToEdit.getId(), request);
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveNewMonthlyExpense(AddMonthlyExpenseRequest request) {
        apiService.addMonthlyExpense(request).enqueue(new Callback<MonthlyExpense>() {
            @Override
            public void onResponse(Call<MonthlyExpense> call, Response<MonthlyExpense> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, "Expense Saved!", Toast.LENGTH_SHORT).show();
                    fetchDashboardData(); // Refresh all data to ensure consistency
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to save.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<MonthlyExpense> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Network Error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateExistingExpense(String expenseId, UpdateExpenseRequest request) {
        apiService.updateExpense(expenseId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, "Expense Updated!", Toast.LENGTH_SHORT).show();
                    fetchDashboardData(); // Refresh all data to ensure consistency
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to update.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Network Error.", Toast.LENGTH_SHORT).show();
            }
        });
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


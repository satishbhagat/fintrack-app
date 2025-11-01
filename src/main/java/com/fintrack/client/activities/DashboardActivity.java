package com.fintrack.client.activities;

import android.app.Dialog;
import android.graphics.Color; // Added import
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
// Import UpdateExpenseRequest if it's used for editing
import com.fintrack.client.models.UpdateExpenseRequest;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
// Import ExpenseSorter if used
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID; // Added import for UUID

// *** Corrected the interface name here ***
public class DashboardActivity extends BaseActivity implements ExpenseAdapter.OnExpenseInteractionListener {

    private static final String TAG = "DashboardActivity";

    private ApiService apiService;
    private ExpenseAdapter expenseAdapter;

    private TextView tvTotalIncome, tvSavings, tvAmountNeeded, tvSelectedMonth, toolbarTitle, tvExpenseListTitle, chartToggle;
    private RecyclerView rvExpenses;
    private PieChart pieChart;
    private ImageButton btnAddExpense;
    private LinearLayout monthSelectorContainer;

    private int selectedYear;
    private int selectedMonth; // 1-12
    private BigDecimal currentTotalIncome = BigDecimal.ZERO;

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
        tvExpenseListTitle = findViewById(R.id.tvExpenseListTitle);
        chartToggle = findViewById(R.id.chart_toggle);

        setupRecyclerView();

        Calendar cal = Calendar.getInstance();
        selectedYear = cal.get(Calendar.YEAR);
        selectedMonth = cal.get(Calendar.MONTH) + 1;

        updateMonthSelectorText();
        fetchDashboardData();

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog(null)); // Pass null for adding new
        monthSelectorContainer.setOnClickListener(v -> showMonthYearPickerDialog());
        chartToggle.setOnClickListener(v -> {
            if (pieChart.getVisibility() == View.GONE) {
                pieChart.setVisibility(View.VISIBLE);
                chartToggle.setText("Hide Chart");
            } else {
                pieChart.setVisibility(View.GONE);
                chartToggle.setText("Show Chart");
            }
        });
    }

    private void showMonthYearPickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_month_year_picker);

        final NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);
        Button btnSelect = dialog.findViewById(R.id.btnSelectMonth);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(new DateFormatSymbols().getMonths());
        monthPicker.setValue(selectedMonth);

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
        tvExpenseListTitle.setText(String.format(Locale.getDefault(), "%s Expenses", monthName));
    }

    private void fetchDashboardData() {
        String emailId = UserSession.getInstance().getEmailId();
        if (emailId == null) {
            Toast.makeText(this, "User session not found. Please log in again.", Toast.LENGTH_LONG).show();
            // Optional: Redirect to login
            return;
        }

        Log.d(TAG, "Fetching dashboard data for " + selectedMonth + "/" + selectedYear);
        apiService.getDashboard(emailId, selectedYear, selectedMonth).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Dashboard data fetched successfully.");
                    updateUI(response.body());
                } else {
                    String errorMsg = "Failed to load dashboard data.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " Error: " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    } else {
                        errorMsg += " Code: " + response.code();
                    }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(DashboardActivity.this, "Failed to load dashboard data.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                Log.e(TAG, "Network Error fetching dashboard data", t);
                Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(DashboardResponse data) {
        Log.d(TAG, "Updating UI with fetched data.");
        currentTotalIncome = data.getTotalIncome() != null ? data.getTotalIncome() : BigDecimal.ZERO;
        tvTotalIncome.setText(String.format(Locale.getDefault(), "₹%.2f", currentTotalIncome));

        List<AbstractExpenseItem> allExpenses = new ArrayList<>();
        if (data.getFixedExpenditures() != null) {
            allExpenses.addAll(data.getFixedExpenditures());
        }
        if (data.getExpenses() != null) {
            allExpenses.addAll(data.getExpenses());
        }

        // Add placeholders for Credit Cards if they don't exist as monthly expenses
        if (data.getCreditCards() != null) {
            for (DashboardResponse.CreditCardItem card : data.getCreditCards()) {
                boolean cardExistsAsExpense = false;
                for (AbstractExpenseItem existingItem : allExpenses) {
                    if (card.getCardName() != null && card.getCardName().equalsIgnoreCase(existingItem.getName())) {
                        cardExistsAsExpense = true;
                        break;
                    }
                }
                if (!cardExistsAsExpense) {
                    DashboardResponse.MonthlyExpenseItem placeholder = new DashboardResponse.MonthlyExpenseItem();
                    placeholder.setId(card.getId());
                    placeholder.setName(card.getCardName());
                    placeholder.setAmount(BigDecimal.ZERO);
                    placeholder.setStatus("PENDING");
                    placeholder.setExpenseMonth(String.format(Locale.ROOT, "%04d-%02d-01", selectedYear, selectedMonth));
                    allExpenses.add(placeholder);
                }
            }
        }

        // Sort before updating adapter
        ExpenseSorter.sortByAmountDescending(allExpenses);

        expenseAdapter.updateExpenses(allExpenses);
        recalculateSummary();
        setReadOnlyMode(checkIfMonthIsDifferent()); // Use the flag from backend
        setupPieChart(allExpenses); // Update chart data
    }

    private boolean checkIfMonthIsDifferent() {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based

        boolean isDifferent = (selectedYear != currentYear) || (selectedMonth != currentMonth);
        Log.d(TAG, "Check if selected month/year is different from current: " + isDifferent);
        return isDifferent;
    }

    private void setReadOnlyMode(boolean isReadOnly) {
        Log.d(TAG, "Setting read-only mode: " + isReadOnly);
        btnAddExpense.setVisibility(isReadOnly ? View.GONE : View.VISIBLE);
        expenseAdapter.setReadOnly(isReadOnly);
    }

    private void setupRecyclerView() {
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), this); // Pass 'this' as the listener
        rvExpenses.setAdapter(expenseAdapter);
    }

    // --- Implementation of OnExpenseInteractionListener ---
    @Override
    public void onStatusChanged() {
        Log.d(TAG, "onStatusChanged triggered. Recalculating summary.");
        recalculateSummary(); // Recalculate totals when status changes
    }

    @Override
    public void onAmountClicked(AbstractExpenseItem item) {
        Log.d(TAG, "onAmountClicked triggered for item: " + (item != null ? item.getName() : "null"));
        // This is called when the amount TextView is clicked in the adapter
        showAddExpenseDialog(item); // Open the edit/add dialog
    }
    // --- End of Listener Implementation ---


    private void recalculateSummary() {
        BigDecimal totalExpensesPaid = BigDecimal.ZERO;
        BigDecimal amountNeeded = BigDecimal.ZERO; // Renamed from totalPendingAmount for clarity

        List<AbstractExpenseItem> currentItems = expenseAdapter.getCurrentExpenses(); // Get current items from adapter
        if (currentItems != null) {
            for (AbstractExpenseItem item : currentItems) {
                BigDecimal itemAmount = (item.getAmount() != null) ? item.getAmount() : BigDecimal.ZERO;

                if ("PAID".equalsIgnoreCase(item.getStatus())) {
                    totalExpensesPaid = totalExpensesPaid.add(itemAmount);
                }
                // Amount needed is the sum of amounts for PENDING items
                if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                    amountNeeded = amountNeeded.add(itemAmount);
                }
            }
        } else {
            Log.w(TAG, "expenseAdapter.getCurrentExpenses() returned null during recalculateSummary");
        }


        // Savings = Total Income - Total *Paid* Expenses
        BigDecimal savings = currentTotalIncome.subtract(totalExpensesPaid);

        tvSavings.setText(String.format(Locale.getDefault(), "₹%.2f", savings));
        tvAmountNeeded.setText(String.format(Locale.getDefault(), "₹%.2f", amountNeeded)); // Display total pending

        Log.d(TAG, "Recalculated Summary - Paid: " + totalExpensesPaid + ", Needed: " + amountNeeded + ", Savings: " + savings);

        // Refresh pie chart with potentially updated data
        setupPieChart(currentItems);
    }

    private void setupPieChart(List<AbstractExpenseItem> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expense data for this month.");
            pieChart.invalidate();
            Log.d(TAG, "Pie chart cleared due to no/empty data.");
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        BigDecimal totalChartAmount = BigDecimal.ZERO;
        for (AbstractExpenseItem expense : expenses) {
            BigDecimal amount = (expense.getAmount() != null) ? expense.getAmount() : BigDecimal.ZERO;
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                entries.add(new PieEntry(amount.floatValue(), expense.getName()));
                totalChartAmount = totalChartAmount.add(amount);
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expenses with amounts > 0 this month.");
            pieChart.invalidate();
            Log.d(TAG, "Pie chart cleared as no entries have amount > 0.");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK); // Use Color.BLACK
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setCenterText("Expenses\n₹" + String.format(Locale.getDefault(), "%.2f", totalChartAmount));
        pieChart.setCenterTextSize(14f);
        pieChart.getLegend().setEnabled(true); // Maybe disable if too cluttered: false
        pieChart.animateY(1000);
        pieChart.invalidate();
        Log.d(TAG, "Pie chart updated with " + entries.size() + " entries.");
    }

    // Modified to accept an item to edit (can be null for adding)
    private void showAddExpenseDialog(final AbstractExpenseItem itemToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        final EditText etExpenseName = dialogView.findViewById(R.id.etExpenseName);
        final EditText etExpenseAmount = dialogView.findViewById(R.id.etExpenseAmount);
        final Button btnSave = dialogView.findViewById(R.id.btnSaveExpense);
        final Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // *** Removed the lines accessing R.id.tvDialogTitle ***

        if (itemToEdit != null) {
            etExpenseName.setText(itemToEdit.getName());
            etExpenseAmount.setText(itemToEdit.getAmount() != null ? itemToEdit.getAmount().toPlainString() : "");
            // Prevent editing name for existing fixed expenses or placeholders
            if (itemToEdit instanceof DashboardResponse.FixedExpenditureItem || itemToEdit.getId() == null || itemToEdit.getId().isEmpty() || "null".equalsIgnoreCase(itemToEdit.getId())) {
                etExpenseName.setEnabled(false);
            } else {
                etExpenseName.setEnabled(true);
            }
        } else {
            etExpenseName.setEnabled(true); // Editable for new expenses
        }

        final AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etExpenseName.getText().toString().trim();
            String amountStr = etExpenseAmount.getText().toString().trim();

            if (name.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    Toast.makeText(this, "Amount cannot be negative.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isNew = (itemToEdit == null);
            // Also treat placeholders (items without a valid ID) as needing creation
            boolean isPlaceholder = !isNew && (itemToEdit.getId() == null || itemToEdit.getId().isEmpty() || "null".equalsIgnoreCase(itemToEdit.getId()));


            if (isNew || isPlaceholder) {
                // Create new expense
                AddMonthlyExpenseRequest request = new AddMonthlyExpenseRequest();
                request.setName(name);
                // Ensure userId is not null and is a valid UUID string
                UUID userId = UserSession.getInstance().getUserId();
                if (userId == null) {
                    Toast.makeText(this, "User session error. Cannot save expense.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "User ID is null in UserSession during save");
                    return;
                }
                request.setUserId(userId.toString()); // Pass UUID as String
                request.setAmount(amount); // Pass BigDecimal directly
                request.setMonth(String.format(Locale.ROOT, "%04d-%02d-01", selectedYear, selectedMonth));
                request.setStatus("PENDING"); // New expenses are pending
                saveNewMonthlyExpense(request);
            } else {
                // Update existing expense
                UpdateExpenseRequest request = new UpdateExpenseRequest();
                request.setAmount(amount); // Pass BigDecimal directly
                // Status is handled by the switch, but if you want to allow changing status here too:
                // request.setStatus(itemToEdit.getStatus()); // Or get from a UI element in dialog
                updateExistingExpense(itemToEdit.getId(), request);
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void saveNewMonthlyExpense(AddMonthlyExpenseRequest request) {
        Log.d(TAG, "Saving new expense: " + request.getName());
        apiService.addMonthlyExpense(request).enqueue(new Callback<MonthlyExpense>() {
            @Override
            public void onResponse(Call<MonthlyExpense> call, Response<MonthlyExpense> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DashboardActivity.this, "Expense added!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "New expense saved successfully, refetching data.");
                    fetchDashboardData(); // Refresh the whole list
                } else {
                    String errorMsg = "Failed to add expense.";
                    if (response.errorBody() != null) {
                        try { errorMsg += " Error: " + response.errorBody().string(); } catch (Exception e) {}
                    } else { errorMsg += " Code: " + response.code(); }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(DashboardActivity.this, "Failed to add expense.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MonthlyExpense> call, Throwable t) {
                Log.e(TAG, "Network error adding expense", t);
                Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateExistingExpense(String expenseId, UpdateExpenseRequest request) {
        Log.d(TAG, "Updating expense ID: " + expenseId);
        if (expenseId == null || expenseId.isEmpty() || "null".equalsIgnoreCase(expenseId)) {
            Log.e(TAG, "Invalid expense ID passed to updateExistingExpense: " + expenseId);
            Toast.makeText(this, "Cannot update expense: Invalid ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.updateExpense(expenseId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, "Expense updated!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Expense updated successfully, refetching data.");
                    fetchDashboardData(); // Refresh the list
                } else {
                    String errorMsg = "Failed to update expense.";
                    if (response.errorBody() != null) {
                        try { errorMsg += " Error: " + response.errorBody().string(); } catch (Exception e) {}
                    } else { errorMsg += " Code: " + response.code(); }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(DashboardActivity.this, "Failed to update expense.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error updating expense", t);
                Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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


package com.fintrack.client.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fintrack.client.R;
import com.fintrack.client.dto.DashboardResponse;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.models.MonthlyExpense;
import com.fintrack.client.models.UpdateExpenseRequest;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<MonthlyExpense> expenses = new ArrayList<>();
    private ApiService apiService;

    public ExpenseAdapter() {
        apiService = RetrofitClient.getInstance().create(ApiService.class);
    }

    public ExpenseAdapter(ArrayList<Object> objects) {
    }

    public void setExpenses(List<MonthlyExpense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        MonthlyExpense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateExpenses(List<DashboardResponse.MonthlyExpenseItem> monthlyExpenses) {
        expenses.clear();
        for (DashboardResponse.MonthlyExpenseItem item : monthlyExpenses) {
            MonthlyExpense expense = new MonthlyExpense();
            expense.id = UUID.fromString(item.getId());
            expense.name = item.getName();
            expense.amount = item.getAmount().doubleValue();
            expense.status = item.getStatus();
            expenses.add(expense);
        }
        notifyDataSetChanged();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        Spinner tvExpenseName;
        EditText etExpenseAmount;
        ToggleButton toggleStatus;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseName = itemView.findViewById(R.id.spinnerExpenseName);
            etExpenseAmount = itemView.findViewById(R.id.etExpenseAmount);
            toggleStatus = itemView.findViewById(R.id.switchStatus);
        }

        void bind(MonthlyExpense expense) {
            tvExpenseName.setSelection(((ArrayAdapter<String>)tvExpenseName.getAdapter()).getPosition(expense.name));
            etExpenseAmount.setText(String.valueOf(expense.amount));
            toggleStatus.setChecked("PAID".equals(expense.status));

            // Listeners
            etExpenseAmount.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    double newAmount = Double.parseDouble(etExpenseAmount.getText().toString());
                    updateExpenseAmount(expense.id.toString(), newAmount);
                }
            });

            toggleStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String newStatus = isChecked ? "PAID" : "PENDING";
                updateExpenseStatus(expense.id.toString(), newStatus);
            });
        }

        private void updateExpenseAmount(String expenseId, double amount) {
            UpdateExpenseRequest request = new UpdateExpenseRequest();
            request.amount = amount;
            request.status = null; // no change
            apiService.updateExpense(expenseId, request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    // implement success here
                    if (response.isSuccessful()) {
                        GenericResponse genericResponse = (GenericResponse) response.body();
                        if (genericResponse != null) {
                            // Optionally handle the response message
                           genericResponse.getMessage();
                        }
                    }

                }

                @Override
                public void onFailure(Call call, Throwable throwable) {
                    // implement failure here
                    throwable.printStackTrace();
                }

            });
        }

        private void updateExpenseStatus(String expenseId, String status) {
            UpdateExpenseRequest request = new UpdateExpenseRequest();
            request.amount = null; // no change
            request.status = status;
            apiService.updateExpense(expenseId, request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    //write implementation here
                    if (response.isSuccessful()) {
                        GenericResponse genericResponse = (GenericResponse) response.body();
                        if (genericResponse != null) {
                            // Optionally handle the response message
                             genericResponse.getMessage();
                        }
                    }
                }

                @Override
                public void onFailure(Call call, Throwable throwable) {
                    //write implementation here
                    throwable.printStackTrace();
                }

            });
        }
    }
}
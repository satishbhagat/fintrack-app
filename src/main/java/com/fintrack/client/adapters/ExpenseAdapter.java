package com.fintrack.client.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fintrack.client.R;
import com.fintrack.client.dto.DashboardResponse;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.models.MonthlyExpense;
import com.fintrack.client.models.UpdateExpenseRequest;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.google.android.material.switchmaterial.SwitchMaterial;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<MonthlyExpense> expenses = new ArrayList<>();
    private ApiService apiService;

    public ExpenseAdapter(ArrayList<MonthlyExpense> expenses) {
        this.expenses = expenses;
        apiService = RetrofitClient.getInstance().create(ApiService.class);
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

        TextView tvExpenseName;
        EditText etExpenseAmount;
        SwitchMaterial switchStatus; // Changed from ToggleButton

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseName = itemView.findViewById(R.id.tvExpenseName);
            etExpenseAmount = itemView.findViewById(R.id.etExpenseAmount);
            switchStatus = itemView.findViewById(R.id.switchStatus); // Correctly cast
        }

        void bind(MonthlyExpense expense) {
            tvExpenseName.setText(expense.name);
            etExpenseAmount.setText(String.valueOf(expense.amount));
            switchStatus.setChecked("PAID".equals(expense.status));

            // Listeners
            etExpenseAmount.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    double newAmount = Double.parseDouble(etExpenseAmount.getText().toString());
                    updateExpenseAmount(expense.id.toString(), newAmount);
                }
            });

            switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String newStatus = isChecked ? "PAID" : "PENDING";
                updateExpenseStatus(expense.id.toString(), newStatus);
            });
        }

        private void updateExpenseAmount(String expenseId, double amount) {
            UpdateExpenseRequest request = new UpdateExpenseRequest();
            request.amount = amount;
            request.status = null; // no change
            apiService.updateExpense(expenseId, request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful()) {
                        // Handle success
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    // Handle failure
                }
            });
        }

        private void updateExpenseStatus(String expenseId, String status) {
            UpdateExpenseRequest request = new UpdateExpenseRequest();
            request.amount = null; // no change
            request.status = status;
            apiService.updateExpense(expenseId, request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful()) {
                        // Handle success
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    // Handle failure
                }
            });
        }
    }
}
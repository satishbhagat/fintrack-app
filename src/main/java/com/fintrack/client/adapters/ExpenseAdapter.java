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

import java.util.List;
import java.util.UUID;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<MonthlyExpense> expenses;
    private ApiService apiService;

    public ExpenseAdapter(List<MonthlyExpense> expenses) {
        this.expenses = expenses;
        apiService = RetrofitClient.getInstance().create(ApiService.class);
    }

    public void setExpenses(List<MonthlyExpense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    public List<MonthlyExpense> getCurrentExpenses() {
        return expenses;
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
        this.expenses.clear();
        for (DashboardResponse.MonthlyExpenseItem item : monthlyExpenses) {
            MonthlyExpense expense = new MonthlyExpense();
            expense.id = UUID.fromString(item.getId());
            expense.name = item.getName();
            expense.amount = item.getAmount().doubleValue();
            expense.status = item.getStatus();
            this.expenses.add(expense);
        }
        notifyDataSetChanged();
    }

    // New method for optimistic update
    public void addExpense(DashboardResponse.MonthlyExpenseItem newItem) {
        MonthlyExpense expense = new MonthlyExpense();
        expense.id = UUID.fromString(newItem.getId());
        expense.name = newItem.getName();
        expense.amount = newItem.getAmount().doubleValue();
        expense.status = newItem.getStatus();

        expenses.add(0, expense); // Add to the top of the list
        notifyItemInserted(0);
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        TextView tvExpenseName;
        EditText etExpenseAmount;
        SwitchMaterial switchStatus;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseName = itemView.findViewById(R.id.tvExpenseName);
            etExpenseAmount = itemView.findViewById(R.id.etExpenseAmount);
            switchStatus = itemView.findViewById(R.id.switchStatus);
        }

        void bind(MonthlyExpense expense) {
            tvExpenseName.setText(expense.name);
            etExpenseAmount.setText(String.valueOf(expense.amount));
            boolean isPaid = "PAID".equalsIgnoreCase(expense.status);
            switchStatus.setChecked(isPaid);
            switchStatus.setText(isPaid ? "Paid" : "Pending");


            switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String newStatus = isChecked ? "PAID" : "PENDING";
                // Update the text immediately for better UX
                switchStatus.setText(newStatus);
                updateExpenseStatus(expense.id.toString(), newStatus);
            });
        }

        private void updateExpenseStatus(String expenseId, String status) {
            UpdateExpenseRequest request = new UpdateExpenseRequest();
            request.status = status;
            apiService.updateExpense(expenseId, request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (!response.isSuccessful()) {
                        // Handle error, maybe revert the switch state
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    // Handle failure, maybe revert the switch state
                }
            });
        }
    }
}


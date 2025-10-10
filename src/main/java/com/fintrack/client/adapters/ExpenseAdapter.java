package com.fintrack.client.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fintrack.client.R;
import com.fintrack.client.dto.DashboardResponse;
import com.fintrack.client.models.AbstractExpenseItem;
import com.fintrack.client.models.UpdateExpenseRequest;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.google.android.material.switchmaterial.SwitchMaterial;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    // Listener interface to communicate with the Activity
    public interface OnExpenseStatusChangedListener {
        void onStatusChanged();
    }

    private List<AbstractExpenseItem> expenses = new ArrayList<>();
    private ApiService apiService;
    private OnExpenseStatusChangedListener statusChangedListener;

    public ExpenseAdapter(ArrayList<AbstractExpenseItem> expenses, OnExpenseStatusChangedListener listener) {
        this.expenses = expenses;
        this.statusChangedListener = listener;
        apiService = RetrofitClient.getInstance().create(ApiService.class);
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        AbstractExpenseItem expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateExpenses(List<DashboardResponse.MonthlyExpenseItem> monthlyExpenses, List<DashboardResponse.FixedExpenditureItem> fixedExpenditures) {
        expenses.clear();
        if (fixedExpenditures != null) {
            expenses.addAll(fixedExpenditures);
        }
        if (monthlyExpenses != null) {
            expenses.addAll(monthlyExpenses);
        }
        notifyDataSetChanged();
    }

    public void addExpense(AbstractExpenseItem expense) {
        this.expenses.add(0, expense);
        notifyItemInserted(0);
    }

    public List<AbstractExpenseItem> getCurrentExpenses() {
        return expenses;
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        TextView tvExpenseName;
        TextView tvExpenseAmount;
        SwitchMaterial switchStatus;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseName = itemView.findViewById(R.id.tvExpenseName);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            switchStatus = itemView.findViewById(R.id.switchStatus);
        }

        void bind(AbstractExpenseItem expense) {
            tvExpenseName.setText(expense.getName());
            tvExpenseAmount.setText(String.format(Locale.getDefault(), "₹%.2f", expense.getAmount().doubleValue()));

            boolean isPaid = "PAID".equalsIgnoreCase(expense.getStatus());
            switchStatus.setChecked(isPaid);
            switchStatus.setText(isPaid ? "Paid" : "Pending");

            // Set listener to null before changing checked state to prevent infinite loops
            switchStatus.setOnCheckedChangeListener(null);
            switchStatus.setChecked(isPaid);

            switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String newStatus = isChecked ? "PAID" : "PENDING";
                expense.setStatus(newStatus);
                switchStatus.setText(newStatus);
                updateExpenseStatus(String.valueOf(expense.getId()), newStatus);

                // Notify the activity that a status has changed
                if (statusChangedListener != null) {
                    statusChangedListener.onStatusChanged();
                }
            });
        }

        private void updateExpenseStatus(String expenseId, String status) {
            if (expenseId == null) return;

            UpdateExpenseRequest request = new UpdateExpenseRequest();
            request.status = status;

            apiService.updateExpense(expenseId, request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(itemView.getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(itemView.getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}


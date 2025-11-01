package com.fintrack.client.adapters;

// *** ADDED this import ***
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fintrack.client.R;
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

    private static final String TAG = "ExpenseAdapter"; // Added TAG for logging
    private List<AbstractExpenseItem> expenses = new ArrayList<>();
    private ApiService apiService;
    private OnExpenseInteractionListener interactionListener; // Keep using this interface
    private boolean isReadOnly = false;

    // Interface remains the same
    public interface OnExpenseInteractionListener {
        void onStatusChanged();
        void onAmountClicked(AbstractExpenseItem item); // This method handles the click
    }

    public ExpenseAdapter(ArrayList<AbstractExpenseItem> expenses, OnExpenseInteractionListener listener) {
        this.expenses = expenses;
        this.interactionListener = listener;
        this.apiService = RetrofitClient.getInstance().create(ApiService.class);
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
        notifyDataSetChanged(); // Use notifyDataSetChanged to redraw all items reflecting read-only state
    }

    public void updateExpenses(List<AbstractExpenseItem> combinedList) {
        this.expenses.clear();
        if (combinedList != null) { // Add null check
            this.expenses.addAll(combinedList);
        }
        notifyDataSetChanged(); // Use notifyDataSetChanged after clearing and adding all
    }

    // Method to add a single expense (if needed after creation)
    public void addExpense(AbstractExpenseItem expense) {
        if (expense != null) {
            this.expenses.add(0, expense); // Add to the top
            notifyItemInserted(0);
        }
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
        // Add null check for safety
        return expenses != null ? expenses.size() : 0;
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

        void bind(final AbstractExpenseItem expense) {
            // Defensive check for null expense item
            if (expense == null) {
                Log.e(TAG, "Binding null expense item at position: " + getAdapterPosition());
                // Optionally hide the view or show an error state
                itemView.setVisibility(View.GONE);
                return;
            } else {
                itemView.setVisibility(View.VISIBLE);
            }

            tvExpenseName.setText(expense.getName());
            // Check if amount is null before formatting
            if (expense.getAmount() != null) {
                tvExpenseAmount.setText(String.format(Locale.getDefault(), "₹%.2f", expense.getAmount().doubleValue()));
            } else {
                tvExpenseAmount.setText("₹0.00"); // Or some placeholder
            }


            boolean isPaid = "PAID".equalsIgnoreCase(expense.getStatus());
            // Set checked state WITHOUT triggering listener temporarily
            switchStatus.setOnCheckedChangeListener(null);
            switchStatus.setChecked(isPaid);
            switchStatus.setText(isPaid ? "Paid" : "Pending");
            switchStatus.setEnabled(!isReadOnly);
            // Re-attach the listener
            switchStatus.setOnCheckedChangeListener((buttonView, isCheckedNow) -> {
                // Check if the change was triggered by user interaction (buttonView.isPressed() might not always work reliably)
                // Use a flag or check against the original state if needed, but often checking if the listener is null is enough initially.
                // For simplicity, we assume user interaction here, but more robust checks can be added.
                Log.d(TAG, "Switch changed by user interaction for: " + expense.getName() + " to " + isCheckedNow);
                String newStatus = isCheckedNow ? "PAID" : "PENDING";
                // Optimistically update local data BEFORE API call
                expense.setStatus(newStatus);
                // Update switch text immediately
                switchStatus.setText(newStatus);
                // Notify activity FIRST to recalculate summary based on optimistic update
                if (interactionListener != null) {
                    interactionListener.onStatusChanged();
                }
                // Call API to update status in the background
                updateExpenseStatus(expense, newStatus, isCheckedNow); // Pass the intended state
            });


            // OnClickListener for the amount TextView
            tvExpenseAmount.setOnClickListener(v -> {
                if (!isReadOnly && interactionListener != null) {
                    interactionListener.onAmountClicked(expense);
                } else if (isReadOnly) {
                    Toast.makeText(itemView.getContext(), "Cannot edit expenses for past months.", Toast.LENGTH_SHORT).show();
                }
            });
            // Make amount text visually distinct if editable
            tvExpenseAmount.setTextColor(isReadOnly ? Color.GRAY : itemView.getContext().getResources().getColor(android.R.color.black)); // Example colors
            // Add long click listener for deletion (example)
            itemView.setOnLongClickListener(view -> {
                if (!isReadOnly && interactionListener != null) {
                    // Implement delete confirmation dialog in Activity/Fragment
                    // interactionListener.onItemLongClicked(expense);
                    Toast.makeText(itemView.getContext(), "Long press detected (Delete action)", Toast.LENGTH_SHORT).show(); // Placeholder
                    return true; // Consume the long click
                }
                return false;
            });

        }

        // Pass the AbstractExpenseItem and the final intended state
        private void updateExpenseStatus(final AbstractExpenseItem expense, final String status, final boolean intendedCheckedState) {
            final String expenseId = expense.getId();

            // Check if expenseId is valid (not null or empty)
            if (expenseId == null || expenseId.isEmpty() || "null".equalsIgnoreCase(expenseId)) {
                Log.w(TAG, "Attempted to update status for item with invalid ID: " + expense.getName());
                // This might be a placeholder item
                // Revert the switch state visually and show a message
                switchStatus.setChecked(!intendedCheckedState); // Toggle back
                switchStatus.setText(!intendedCheckedState ? "Paid" : "Pending"); // Update text accordingly
                Toast.makeText(itemView.getContext(), "Cannot change status until amount is set.", Toast.LENGTH_SHORT).show();
                // Revert local status data
                expense.setStatus(!intendedCheckedState ? "PAID" : "PENDING");
                // Notify listener to reset calculations
                if (interactionListener != null) {
                    interactionListener.onStatusChanged();
                }
                return;
            }


            UpdateExpenseRequest request = new UpdateExpenseRequest();
            request.status = status;

            Log.d(TAG, "Calling API to update status for expense ID: " + expenseId + " to " + status);
            apiService.updateExpense(expenseId, request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "API Error updating status for " + expenseId + ": " + response.code() + " " + response.message());
                        Toast.makeText(itemView.getContext(), "Failed to update status (API Error).", Toast.LENGTH_SHORT).show();
                        // Revert UI and data on API failure
                        expense.setStatus(!status.equals("PAID") ? "PAID" : "PENDING"); // Revert local data
                        // Set checked state WITHOUT triggering listener
                        switchStatus.setOnCheckedChangeListener(null);
                        switchStatus.setChecked(!intendedCheckedState); // Revert switch UI
                        switchStatus.setText(!intendedCheckedState ? "Paid" : "Pending"); // Revert text
                        // Re-attach listener
                        switchStatus.setOnCheckedChangeListener((buttonView, isCheckedNow) -> { /* Re-add listener logic if needed */ });
                        if (interactionListener != null) {
                            interactionListener.onStatusChanged(); // Recalculate with reverted state
                        }
                    } else {
                        Log.d(TAG, "Successfully updated status via API for expense ID: " + expenseId);
                        // Success - UI is already updated optimistically
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Network Error updating status for " + expenseId, t);
                    Toast.makeText(itemView.getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    // Revert UI and data on network failure
                    expense.setStatus(!status.equals("PAID") ? "PAID" : "PENDING");
                    // Set checked state WITHOUT triggering listener
                    switchStatus.setOnCheckedChangeListener(null);
                    switchStatus.setChecked(!intendedCheckedState); // Revert switch UI
                    switchStatus.setText(!intendedCheckedState ? "Paid" : "Pending"); // Revert text
                    // Re-attach listener
                    switchStatus.setOnCheckedChangeListener((buttonView, isCheckedNow) -> { /* Re-add listener logic if needed */ });
                    if (interactionListener != null) {
                        interactionListener.onStatusChanged(); // Recalculate with reverted state
                    }
                }
            });
        }
    }
}


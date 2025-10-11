package com.fintrack.client.dto;

import com.fintrack.client.models.AbstractExpenseItem;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class DashboardResponse {

    @SerializedName("monthExpenses")
    private List<MonthlyExpenseItem> expenses;

    @SerializedName("fixedExpenditures")
    private List<FixedExpenditureItem> fixedExpenditures;

    @SerializedName("totalExpenses")
    private BigDecimal totalExpenses;

    @SerializedName("amountRemaining")
    private BigDecimal amountRemaining;

    @SerializedName("pendingAmount")
    private BigDecimal pendingAmount;

    @SerializedName("monthlySalry")
    private BigDecimal monthlySalry;

    @SerializedName("totalIncome")
    private BigDecimal totalIncome;

    @SerializedName("isEditable")
    private boolean isEditable; // New field to control UI state


    // Getters and setters
    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public List<MonthlyExpenseItem> getExpenses() { return expenses; }
    public void setExpenses(List<MonthlyExpenseItem> expenses) { this.expenses = expenses; }

    public List<FixedExpenditureItem> getFixedExpenditures() { return fixedExpenditures; }
    public void setFixedExpenditures(List<FixedExpenditureItem> fixedExpenditures) { this.fixedExpenditures = fixedExpenditures; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getAmountRemaining() { return amountRemaining; }
    public void setAmountRemaining(BigDecimal amountRemaining) { this.amountRemaining = amountRemaining; }

    public BigDecimal getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }

    public BigDecimal getMonthlySalry() { return monthlySalry; }
    public void setMonthlySalry(BigDecimal monthlySalry) { this.monthlySalry = monthlySalry; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    /**
     * Inner class representing each monthly expense item.
     */
    public static class MonthlyExpenseItem extends AbstractExpenseItem {

        @SerializedName("id")
        private UUID id;

        @SerializedName("name")
        private String name;

        @SerializedName("amount")
        private BigDecimal amount;

        @SerializedName("status")
        private String status;

        // Other fields specific to MonthlyExpenseItem
        @SerializedName("expenseMonth")
        private String expenseMonth;

        @SerializedName("category")
        private String category;

        @SerializedName("creditCardId")
        private String creditCardId;

        // Implemented methods from AbstractExpenseItem
        @Override
        public UUID getId() { return this.id; }
        @Override
        public void setId(UUID id) { this.id = id; }

        @Override
        public String getName() { return name; }
        @Override
        public void setName(String name) { this.name = name; }

        @Override
        public BigDecimal getAmount() { return amount; }
        @Override
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        @Override
        public String getStatus() { return status; }
        @Override
        public void setStatus(String status) { this.status = status; }

        // Getters and setters for specific fields
        public String getExpenseMonth() { return expenseMonth; }
        public void setExpenseMonth(String expenseMonth) { this.expenseMonth = expenseMonth; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getCreditCardId() { return creditCardId; }
        public void setCreditCardId(String creditCardId) { this.creditCardId = creditCardId; }
    }

    /**
     * Inner class representing each fixed expenditure item.
     */
    public static class FixedExpenditureItem extends AbstractExpenseItem {

        @SerializedName("id")
        private UUID id;

        @SerializedName("name")
        private String name;

        @SerializedName("amount")
        private BigDecimal amount;

        @SerializedName("status")
        private String status;

        // Implemented methods from AbstractExpenseItem
        @Override
        public UUID getId() { return id; }
        @Override
        public void setId(UUID id) { this.id = id; }

        @Override
        public String getName() { return name; }
        @Override
        public void setName(String name) { this.name = name; }

        @Override
        public BigDecimal getAmount() { return amount; }
        @Override
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        @Override
        public String getStatus() { return status; }
        @Override
        public void setStatus(String status) { this.status = status; }
    }
}


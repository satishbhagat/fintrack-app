package com.fintrack.client.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    @SerializedName("expenses")
    private List<MonthlyExpenseItem> expenses;

    @SerializedName("totalExpenses")
    private BigDecimal totalExpenses;

    @SerializedName("amountRemaining")
    private BigDecimal amountRemaining;

    @SerializedName("pendingAmount")
    private BigDecimal pendingAmount;

    @SerializedName("monthlySalry") // Note the spelling matches JSON
    private BigDecimal monthlySalry;
    private BigDecimal totalIncome;

    // Getters and setters
    public List<MonthlyExpenseItem> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<MonthlyExpenseItem> expenses) {
        this.expenses = expenses;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getAmountRemaining() {
        return amountRemaining;
    }

    public void setAmountRemaining(BigDecimal amountRemaining) {
        this.amountRemaining = amountRemaining;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public BigDecimal getMonthlySalry() {
        return monthlySalry;
    }

    public void setMonthlySalry(BigDecimal monthlySalry) {
        this.monthlySalry = monthlySalry;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }
    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    /**
     * Inner class representing each expense item.
     */
    public static class MonthlyExpenseItem {

        @SerializedName("id")
        private String id; // UUID as String

        @SerializedName("name")
        private String name;

        @SerializedName("amount")
        private BigDecimal amount;

        @SerializedName("expenseMonth")
        private String expenseMonth; // date string

        @SerializedName("status")
        private String status; // "PENDING" or "PAID"

        @SerializedName("category")
        private String category; // can be null

        @SerializedName("creditCardId")
        private String creditCardId; // can be null

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getExpenseMonth() { return expenseMonth; }
        public void setExpenseMonth(String expenseMonth) { this.expenseMonth = expenseMonth; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getCreditCardId() { return creditCardId; }
        public void setCreditCardId(String creditCardId) { this.creditCardId = creditCardId; }
    }
}
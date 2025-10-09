package com.fintrack.client.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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

    @SerializedName("monthlySalry") // Note the spelling matches JSON
    private BigDecimal monthlySalry;
    private BigDecimal totalIncome;

    // Getters and setters


    public List<FixedExpenditureItem> getFixedExpenditures() {
        return fixedExpenditures;
    }

    public void setFixedExpenditures(List<FixedExpenditureItem> fixedExpenditures) {
        this.fixedExpenditures = fixedExpenditures;
    }

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
     * Inner class representing fixed expense item.
     */

    public static class FixedExpenditureItem {
        @SerializedName("id")
        private String id;
        @SerializedName("name")
        private String name;
        @SerializedName("amount")
        private Double amount;
        @SerializedName("createdAt")
        private OffsetDateTime createdAt;

        @SerializedName("active")
        private boolean active;

        @SerializedName("userId")
        private UUID userId;

        // Getters and setters

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public OffsetDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }
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
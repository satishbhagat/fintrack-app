package com.fintrack.client.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class DashboardRequest {
    String emailId;

    @SerializedName("expenses")
    private List<MonthlyExpenseItem> expenses;

    public List<MonthlyExpenseItem> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<MonthlyExpenseItem> expenses) {
        this.expenses = expenses;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public static class MonthlyExpenseItem {

        @SerializedName("name")
        private String name;

        @SerializedName("amount")
        private BigDecimal amount;
        @SerializedName("status")
        private String status; // "PENDING" or "PAID"

        // Getters and setters

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }


    }
}

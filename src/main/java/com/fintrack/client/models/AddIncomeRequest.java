package com.fintrack.client.models;

import java.math.BigDecimal;

public class AddIncomeRequest {
    private BigDecimal amount;
    private String description;
    private String incomeMonth; // e.g., "2025-09-01"
    private boolean isRecurring;
    private String userId;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIncomeMonth() { return incomeMonth; }
    public void setIncomeMonth(String incomeMonth) { this.incomeMonth = incomeMonth; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}


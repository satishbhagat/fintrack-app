package com.fintrack.client.models;

import java.math.BigDecimal;

public class AddFixedExpenditureRequest {
    private String name;
    private BigDecimal amount;
    private int due_date;
    private String userId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getDueDate() { return due_date; }
    public void setDueDate(int due_date) { this.due_date = due_date; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}


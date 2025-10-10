package com.fintrack.client.models;

import java.math.BigDecimal;
import java.util.UUID;

public class MonthlyExpense extends AbstractExpenseItem{
    public UUID id;
    public UUID userId;
    public String name;
    public BigDecimal amount;
    public String expenseMonth; // e.g., "2025-09-01"
    public String status; // "PAID" or "PENDING"
    public String category;
    public UUID creditCardId; // nullable
    public String createdAt;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public BigDecimal getAmount() {
        return this.amount;
    }

    @Override
    public String getStatus() {
        return this.status;
    }

    @Override
    public void setId(UUID id) {
        this.id =id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }
}
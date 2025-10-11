package com.fintrack.client.models;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;
@Data
public class ExtraIncome {
    public UUID id;
    public UUID userId;
    public double amount;
    public String description;
    public String incomeMonth; // e.g., "2025-09-01"
    public boolean isRecurring;
    public Instant createdAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIncomeMonth(String incomeMonth) {
        this.incomeMonth = incomeMonth;
    }
}

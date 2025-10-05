package com.fintrack.client.models;

import java.util.UUID;

public class MonthlyExpense {
    public UUID id;
    public UUID userId;
    public String name;
    public double amount;
    public String expenseMonth; // e.g., "2025-09-01"
    public String status; // "PAID" or "PENDING"
    public String category;
    public UUID creditCardId; // nullable
    public String createdAt;
}
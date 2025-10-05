package com.fintrack.client.models;

public class AddMonthlyExpenseRequest {
    public String name;
    public double amount;
    public String month; // e.g., "2025-09-01"
    public String category;
}
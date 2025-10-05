package com.fintrack.client.models;

import lombok.Data;

import java.util.UUID;

//lombok getters and setter
@Data
public class FixedExpenditure {
    public UUID id;
    public UUID userId;
    public String name;
    public double amount;
    public int dueDate; // 1-31
    public boolean isActive;
    public String createdAt;
}
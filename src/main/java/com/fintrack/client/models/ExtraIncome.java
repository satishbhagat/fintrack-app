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
    public Instant createdAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
package com.fintrack.client.models;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateExpenseRequest {
    public UUID uuid;

    public BigDecimal amount;
    public String status; // "PAID" or "PENDING"

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
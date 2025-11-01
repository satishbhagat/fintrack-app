package com.fintrack.client.models;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SavingsGoal {
    private UUID id;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private String targetDate;
    private BigDecimal monthlyContribution;
    private UUID userId;

    public void setMonthlyContribution(BigDecimal monthlyContribution) {
        this.monthlyContribution= monthlyContribution;
    }

    public BigDecimal getMonthlyContribution() {
        return monthlyContribution;
    }
}

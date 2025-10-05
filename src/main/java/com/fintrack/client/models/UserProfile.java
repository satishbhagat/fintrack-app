package com.fintrack.client.models;

import java.util.List;

public class UserProfile {
    public String email;
    public double monthly_salary;
    public List<FixedExpenditure> fixed_expenditures;
    public List<CreditCard> credit_cards;
}
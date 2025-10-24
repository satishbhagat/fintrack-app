package com.fintrack.client.dto;


import com.google.gson.annotations.SerializedName; // Added this import
import java.math.BigDecimal;
import java.util.List;

public class SpendDataResponse {

    @SerializedName("fixedExpenditures")
    private List<FixedExpenditureItem> fixedExpenditures;

    @SerializedName("creditCards")
    private List<CreditCardItem> creditCards;

    @SerializedName("extraIncome")
    private List<ExtraIncomeItem> extraIncome;

    // Getters and Setters...
    public List<FixedExpenditureItem> getFixedExpenditures() {
        return fixedExpenditures;
    }
    public void setFixedExpenditures(List<FixedExpenditureItem> fixedExpenditures) {
        this.fixedExpenditures = fixedExpenditures;
    }
    public List<CreditCardItem> getCreditCards() {
        return creditCards;
    }
    public void setCreditCards(List<CreditCardItem> creditCards) {
        this.creditCards = creditCards;
    }
    public List<ExtraIncomeItem> getExtraIncome() {
        return extraIncome;
    }
    public void setExtraIncome(List<ExtraIncomeItem> extraIncome) {
        this.extraIncome = extraIncome;
    }

    // Inner class for Fixed Expenditure items
    public static class FixedExpenditureItem {
        @SerializedName("id") // Ensure GSON maps the ID
        private String id;
        @SerializedName("name")
        private String name;
        @SerializedName("amount")
        private BigDecimal amount;

        // Getters for id, name, amount
        public String getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getAmount() { return amount; }
    }

    // Inner class for Credit Card items
    public static class CreditCardItem {
        @SerializedName("id") // Ensure GSON maps the ID
        private String id;
        @SerializedName("cardName")
        private String cardName;
        @SerializedName("dueDate")
        private String dueDate; // e.g., "15th"

        // Getters for id, cardName, dueDate
        public String getId() { return id; }
        public String getCardName() { return cardName; }
        public String getDueDate() { return dueDate; }
    }

    // Inner class for Extra Income items
    public static class ExtraIncomeItem {
        @SerializedName("id") // Ensure GSON maps the ID
        private String id;
        @SerializedName("amount")
        private BigDecimal amount;
        @SerializedName("description")
        private String description;
        @SerializedName("incomeMonth")
        private String incomeMonth;
        @SerializedName("isRecurring")
        private boolean isRecurring;

        // Getters for id, amount, description, incomeMonth, isRecurring
        public String getId() { return id; }
        public BigDecimal getAmount() { return amount; }
        public String getDescription() { return description; }
        public String getIncomeMonth() { return incomeMonth; }
        public boolean isRecurring() { return isRecurring; }
    }
}


package com.fintrack.client.dto;


import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class SpendDataResponse {

    @SerializedName("fixedExpenditures")
    private List<FixedExpenditureItem> fixedExpenditures;

    @SerializedName("creditCards")
    private List<CreditCardItem> creditCards;

    public List<FixedExpenditureItem> getFixedExpenditures() {
        return fixedExpenditures;
    }

    public List<CreditCardItem> getCreditCards() {
        return creditCards;
    }

    // Inner class for Fixed Expenditure items
    public static class FixedExpenditureItem {
        @SerializedName("name")
        private String name;

        @SerializedName("amount")
        private BigDecimal amount;

        public String getName() {
            return name;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }

    // Inner class for Credit Card items
    public static class CreditCardItem {
        @SerializedName("cardName")
        private String cardName;

        @SerializedName("dueDate")
        private String dueDate; // e.g., "15th"

        public String getCardName() {
            return cardName;
        }

        public String getDueDate() {
            return dueDate;
        }
    }
}


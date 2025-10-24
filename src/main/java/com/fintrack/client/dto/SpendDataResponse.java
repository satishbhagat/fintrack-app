package com.fintrack.client.dto;


import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class SpendDataResponse {

    @SerializedName("fixedExpenditures")
    private List<FixedExpenditureItem> fixedExpenditures;

    @SerializedName("creditCards")
    private List<CreditCardItem> creditCards;

    @SerializedName("extraIncome")
    private List<ExtraIncomeItem> extraIncome;

    public List<FixedExpenditureItem> getFixedExpenditures() {
        return fixedExpenditures;
    }

    public List<CreditCardItem> getCreditCards() {
        return creditCards;
    }


    public void setFixedExpenditures(List<FixedExpenditureItem> fixedExpenditures) {
        this.fixedExpenditures = fixedExpenditures;
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
        @SerializedName("name")
        private String name;

        @SerializedName("id")
        private String id;

        @SerializedName("amount")
        private BigDecimal amount;

        public String getName() {
            return name;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getId() {
            return id;
        }
    }

    // Inner class for Credit Card items
    public static class CreditCardItem {
        @SerializedName("cardName")
        private String cardName;

        @SerializedName("id")
        private String id;

        @SerializedName("dueDate")
        private String dueDate; // e.g., "15th"

        public String getCardName() {
            return cardName;
        }

        public String getDueDate() {
            return dueDate;
        }

        public String getId() {
            return id;
        }
    }

    // Inner class for Extra Income items
    public static class ExtraIncomeItem {

        @SerializedName("id")
        private String id;

        @SerializedName("amount")
        private BigDecimal amount;

        @SerializedName("description")
        private String description;

        public BigDecimal getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }

        public String getId() {
            return id;
        }
    }
}


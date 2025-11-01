package com.fintrack.client.models;

public class AddCreditCardRequest {
    private String cardName;
    private int dueDate;
    private String userId;

    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }

    public int getDueDate() { return dueDate; }
    public void setDueDate(int dueDate) { this.dueDate = dueDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}


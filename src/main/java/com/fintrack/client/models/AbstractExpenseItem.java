package com.fintrack.client.models;

import java.math.BigDecimal;

/**
 * An abstract base class to represent a generic expense item.
 * This allows different types of expenses (e.g., monthly, fixed)
 * to be treated uniformly in lists and adapters.
 */
public abstract class AbstractExpenseItem {

    // Abstract methods to ensure subclasses implement these getters
    public abstract String getId();
    public abstract String getName();
    public abstract BigDecimal getAmount();
    public abstract String getStatus();

    // Abstract methods to ensure subclasses implement these setters
    public abstract void setId(String id);
    public abstract void setName(String name);
    public abstract void setAmount(BigDecimal amount);
    public abstract void setStatus(String status);
}


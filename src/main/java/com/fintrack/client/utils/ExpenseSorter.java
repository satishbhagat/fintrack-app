package com.fintrack.client.utils;

import com.fintrack.client.models.AbstractExpenseItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExpenseSorter {

    public static void sortByAmountDescending(List<? extends AbstractExpenseItem> expenseItems) {
        Collections.sort(expenseItems, new Comparator<AbstractExpenseItem>() {
            @Override
            public int compare(AbstractExpenseItem item1, AbstractExpenseItem item2) {
                return item2.getAmount().compareTo(item1.getAmount()); // descending order
            }
        });
    }
}

package com.fintrack.client.dto;


import android.os.Build;
import com.fintrack.client.models.FixedExpenditure;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for representing a fixed expenditure record in API responses.
 */
@Data
public class FixedExpenditureResponse {
    private UUID id;
    private String name;
    private BigDecimal amount;
    private int dueDate;
    private boolean isActive;
    private Instant createdAt;

    /**
     * A static factory method to convert a FixedExpenditure entity to its DTO representation.
     * @param fixedExpenditure The FixedExpenditure entity from the database.
     * @return A new FixedExpenditureResponse object.
     */
    public static FixedExpenditureResponse fromEntity(FixedExpenditure fixedExpenditure) {
        FixedExpenditureResponse response = new FixedExpenditureResponse();
        response.setId(fixedExpenditure.getId());
        response.setName(fixedExpenditure.getName());
        response.setAmount(BigDecimal.valueOf(fixedExpenditure.getAmount()));
        response.setDueDate(fixedExpenditure.getDueDate());
        response.setActive(fixedExpenditure.isActive());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            response.setCreatedAt(Instant.parse(fixedExpenditure.getCreatedAt()));
        }
        return response;
    }
}


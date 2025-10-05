package com.fintrack.client.dto;


import android.os.Build;
import com.fintrack.client.models.ExtraIncome;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for representing an extra income record in API responses.
 * This ensures that only relevant and safe-to-expose data is sent to the client.
 */
@Data
public class IncomeResponse {
    private UUID id;
    private BigDecimal amount;
    private String description;
    private LocalDate incomeMonth;
    private Instant createdAt;

    /**
     * A static factory method to easily convert an ExtraIncome entity to an IncomeResponse DTO.
     * This is a common pattern to keep conversion logic separate from the models themselves.
     * @param extraIncome The ExtraIncome entity from the database.
     * @return A new IncomeResponse object.
     */
    public static IncomeResponse fromEntity(ExtraIncome extraIncome) {
        IncomeResponse response = new IncomeResponse();
        response.setId(extraIncome.getId());
        response.setAmount(BigDecimal.valueOf(extraIncome.getAmount()));
        response.setDescription(extraIncome.getDescription());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            response.setIncomeMonth(LocalDate.parse(extraIncome.getIncomeMonth()));
        }
        response.setCreatedAt(extraIncome.getCreatedAt());
        return response;
    }
}


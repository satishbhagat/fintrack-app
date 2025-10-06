package com.fintrack.client.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ProfileSetupRequest {

    private String emailId;
    private BigDecimal monthlySalary;
    private List<FixedExpenditureDto> fixedExpenditures;
    private List<CreditCardDto> creditCards;

    @Data
    public static class FixedExpenditureDto {
        private String name;
        private BigDecimal amount;
    }

    @Data
    public static class CreditCardDto {
        private String cardName;
        private UUID userId;
    }
}

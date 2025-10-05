package com.fintrack.client.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProfileSetupRequest {

    private BigDecimal monthlySalary;
    private List<FixedExpenditureDto> fixedExpenditures;
    private List<String> creditCardNames;

    @Data
    public static class FixedExpenditureDto {
        private String name;
        private BigDecimal amount;
    }
}

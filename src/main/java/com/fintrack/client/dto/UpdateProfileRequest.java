package com.fintrack.client.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for handling requests to update a user's profile.
 * Carries the data from the client for the PUT /profile endpoint.
 */
@Data
public class UpdateProfileRequest {

    /**
     * The new monthly salary for the user.
     * Using BigDecimal for financial calculations to ensure precision.
     */
    private BigDecimal monthlySalary;
}

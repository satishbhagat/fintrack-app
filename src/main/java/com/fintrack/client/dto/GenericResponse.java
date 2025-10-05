package com.fintrack.client.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generic response object used for simple API responses,
 * typically for operations that don't need to return a full data object (e.g., updates or deletes).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse {

    /**
     * A boolean flag indicating the outcome of the operation.
     * True for success, false for failure.
     */
    private boolean success;

    /**
     * A message providing more context about the response,
     * such as "Profile updated successfully" or an error description.
     */
    private String message;
}

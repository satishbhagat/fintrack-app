package com.fintrack.client.dto;

import java.util.UUID;

public class AuthResponse {
    private UUID userId; // UUID as String
    private String email;
    private Double salary;

    private String message;

    public AuthResponse() { }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getSalary() {
        return salary;
    }
    public void setSalary(Double salary) {
        this.salary = salary;
    }


    public void setMessage(String loginSuccessful) {
        this.message = loginSuccessful;
    }
    public String getMessage() {
        return message;
    }
}
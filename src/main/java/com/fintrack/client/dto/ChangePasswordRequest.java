package com.fintrack.client.dto;


public class ChangePasswordRequest {
    private String email;
    private String currentPassword;
    private String newPassword;

    public ChangePasswordRequest(String email, String currentPassword, String newPassword) {
        this.email = email;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    // Getters are needed for Gson serialization
    public String getEmail() {
        return email;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}

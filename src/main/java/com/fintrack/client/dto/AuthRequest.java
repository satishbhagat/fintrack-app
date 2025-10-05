package com.fintrack.client.dto;


import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object for sending user credentials during login.
 * This class is serialized into a JSON object for the API request body.
 */
public class AuthRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters are needed for serialization, but setters are not required for this DTO.
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}


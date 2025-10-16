package com.fintrack.client.models;

public class LinkAccountRequest {
    public String publicToken;
    public String institutionName;
    public String userId;

    public LinkAccountRequest(String publicToken, String institutionName, String userId) {
        this.publicToken = publicToken;
        this.institutionName = institutionName;
        this.userId = userId;
    }
}

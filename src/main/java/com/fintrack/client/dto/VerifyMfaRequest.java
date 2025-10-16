package com.fintrack.client.dto;

import lombok.Data;

@Data
public class VerifyMfaRequest {
    private String username;
    private String code;
}

package com.fintrack.client.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class MfaSetupResponse {
    @SerializedName("secret")
    private String secret;
    @SerializedName("qrCodeUrl")
    private String qrCodeUrl;
}

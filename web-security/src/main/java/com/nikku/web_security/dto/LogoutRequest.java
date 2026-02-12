package com.nikku.web_security.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}

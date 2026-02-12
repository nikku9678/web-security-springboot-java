package com.nikku.web_security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenService refreshTokenService;

    public void logout(String refreshToken) {

        refreshTokenService.revokeToken(refreshToken);
    }
}

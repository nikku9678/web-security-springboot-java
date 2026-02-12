package com.nikku.web_security.controller;

import com.nikku.web_security.dto.*;
import com.nikku.web_security.entity.RefreshToken;
import com.nikku.web_security.entity.User;
import com.nikku.web_security.security.AuthUtil;
import com.nikku.web_security.service.AuthService;
import com.nikku.web_security.service.LogoutService;
import com.nikku.web_security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final AuthUtil authUtil;
    private final LogoutService logoutService;
    private final RefreshTokenService refreshTokenService;

    // 🔹 Register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                authService.register(request)
        );
    }

    // 🔹 Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        AuthResponse res = authService.login(request);

        return ResponseEntity.ok(
                res
        );
    }



    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.verifyToken(
                        request.getRefreshToken());

        User user = refreshToken.getUser();

        String newAccessToken =
                authUtil.generateAccessToken(user);

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(request.getRefreshToken())
                        .username(user.getUsername())
                        .build()
        );
    }



    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody LogoutRequest request) {

        logoutService.logout(request.getRefreshToken());

        return ResponseEntity.ok("Logged out successfully");
    }

}

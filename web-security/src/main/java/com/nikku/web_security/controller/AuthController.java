package com.nikku.web_security.controller;

import com.nikku.web_security.dto.*;
import com.nikku.web_security.entity.RefreshToken;
import com.nikku.web_security.entity.User;
import com.nikku.web_security.security.AuthUtil;
import com.nikku.web_security.service.AuthService;
import com.nikku.web_security.service.LogoutService;
import com.nikku.web_security.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LogoutService logoutService;
    private final RefreshTokenService refreshTokenService;
    private final AuthUtil authUtil;

    // Register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> signUp(
            @RequestBody RegisterRequest request) {

        return new ResponseEntity<>(
                authService.register(request),
                HttpStatus.CREATED);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> logIn(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request);

        // Store refresh token in HttpOnly cookie
        Cookie cookie = new Cookie("refreshToken", authResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days

        response.addCookie(cookie);

        return ResponseEntity.ok(authResponse);
    }

    // Refresh Token
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();

        String newAccessToken = authUtil.createAccessToken(user);

        return new AuthResponse(newAccessToken,request.getRefreshToken(),user.getUsername(),"Access token generated successfully..." );
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found"));

        authService.logout(refreshToken);

        // Remove cookie
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }
}
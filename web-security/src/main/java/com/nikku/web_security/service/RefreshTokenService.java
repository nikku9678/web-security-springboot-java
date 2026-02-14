package com.nikku.web_security.service;

import com.nikku.web_security.entity.RefreshToken;
import com.nikku.web_security.entity.User;
import com.nikku.web_security.repository.RefreshTokenRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    // 🔹 Create Refresh Token
    public RefreshToken createRefreshToken(User user, String tokenValue) {

        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        return repository.save(token);
    }

    // 🔹 Verify Refresh Token
    public RefreshToken verifyRefreshToken(String tokenValue) {

        RefreshToken refreshToken = repository.findByToken(tokenValue)
                .orElseThrow(() ->
                        new AuthenticationServiceException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new AuthenticationServiceException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AuthenticationServiceException("Refresh token expired");
        }

        return refreshToken;
    }

    // 🔹 Revoke Token (Logout)
    @Transactional
    public void revokeToken(String tokenValue) {

        RefreshToken token = repository.findByToken(tokenValue)
                .orElseThrow(() ->
                        new AuthenticationServiceException("Refresh token not found"));

        token.setRevoked(true);

        // No need to call save() explicitly inside @Transactional
    }

    // 🔹 Optional: Delete all tokens of a user (Logout from all devices)
    // @Transactional
    // public void revokeAllUserTokens(User user) {
    //     repository.findAllByUser(user)
    //             .forEach(token -> token.setRevoked(true));
    // }

    // // 🔹 Optional: Clean expired tokens (can be scheduled)
    // @Transactional
    // public void deleteExpiredTokens() {
    //     repository.deleteByExpiryDateBefore(LocalDateTime.now());
    // }
}

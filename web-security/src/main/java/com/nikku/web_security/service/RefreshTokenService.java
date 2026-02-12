package com.nikku.web_security.service;

import com.nikku.web_security.entity.RefreshToken;
import com.nikku.web_security.entity.User;
import com.nikku.web_security.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public RefreshToken createRefreshToken(
            User user,
            String tokenValue) {

        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        return repository.save(token);
    }

    public RefreshToken verifyToken(String token) {

        RefreshToken refreshToken =
                repository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Token revoked");
        }

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        return refreshToken;
    }


        @Transactional
        public void revokeToken(String refreshToken) {

            RefreshToken token = repository
                    .findByToken(refreshToken)
                    .orElseThrow(() ->
                            new RuntimeException("Token not found"));

            token.setRevoked(true);

            repository.save(token);

            System.out.println("TOKEN REVOKED");
        }
}

package com.nikku.web_security.security;

import com.nikku.web_security.config.JWTProperties;
import com.nikku.web_security.entity.User;
import com.nikku.web_security.entity.type.AuthProviderType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.Access;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthUtil {

    private final JWTProperties jwtProperties;

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey generateSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    // ACCESS TOKEN (short lived)
    public String createAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("roles", user.getAuthorities())
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() +
                                jwtProperties.getAccessTokenExpiration()) // Access token from properties file
                )
                .signWith(generateSecretKey())
                .compact();
    }

    // REFRESH TOKEN (long lived)
    public String createRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration()))  // Refresh token from properties file
                .signWith(generateSecretKey())
                .compact();
    }

    // Extract UserId safely
    public Long generateUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(generateSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }
}
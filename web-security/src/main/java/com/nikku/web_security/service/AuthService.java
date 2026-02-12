package com.nikku.web_security.service;

import com.nikku.web_security.dto.*;
import com.nikku.web_security.entity.Role;
import com.nikku.web_security.entity.User;
import com.nikku.web_security.repository.RoleRepository;
import com.nikku.web_security.repository.UserRepository;
import com.nikku.web_security.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;

    // ✅ ADD THIS
    private final RefreshTokenService refreshTokenService;

    // 🔹 Register
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByUsername(
                request.getUsername()).isPresent()) {
            throw new RuntimeException(
                    "Username already exists");
        }

        Role userRole = roleRepository
                .findByName("ROLE_USER")
                .orElseThrow(() ->
                        new RuntimeException(
                                "ROLE_USER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder
                        .encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);

        // 🔐 Generate Tokens
        String accessToken =
                authUtil.generateAccessToken(user);

        String refreshToken =
                authUtil.generateRefreshToken(user);

        // 💾 Save refresh token
        refreshTokenService
                .createRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .build();
    }

    // 🔹 Login

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow();

        // 🔐 Generate access token
        String accessToken =
                authUtil.generateAccessToken(user);

        // 🔐 Generate refresh token
        String refreshToken =
                authUtil.generateRefreshToken(user);

        // 💾 Store refresh token in DB
        refreshTokenService
                .createRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .build();
    }


}

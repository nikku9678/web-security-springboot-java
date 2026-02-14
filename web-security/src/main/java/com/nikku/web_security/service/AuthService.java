package com.nikku.web_security.service;

import com.nikku.web_security.dto.*;
import com.nikku.web_security.entity.Role;
import com.nikku.web_security.entity.User;
import com.nikku.web_security.entity.RefreshToken;
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
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;

    // 🔹 Register
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Role userRole = roleRepository
                .findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);

        return generateTokens(user);
    }

    // 🔹 Login
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow();

        return generateTokens(user);
    }

    // 🔹 Refresh
    public AuthResponse refreshToken(String refreshTokenValue) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(refreshTokenValue);

        User user = refreshToken.getUser();

        String newAccessToken =
                authUtil.createAccessToken(user);

        sessionService.validateSession(refreshTokenValue);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue)
                .username(user.getUsername())
                .build();
    }

    // 🔹 Helper method
    private AuthResponse generateTokens(User user) {

        String accessToken =
                authUtil.createAccessToken(user);

        String refreshToken =
                authUtil.createRefreshToken(user);

        refreshTokenService.createRefreshToken(user, refreshToken);

        sessionService.generateNewSession(user,refreshToken);  //generate new session

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .message("Register user Successfully...")
                .build();
    }



    public void logout(String refreshToken) {

        refreshTokenService.revokeToken(refreshToken);
    }
}

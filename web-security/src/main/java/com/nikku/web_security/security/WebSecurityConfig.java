package com.nikku.web_security.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers("/auth/**").permitAll()

                        // Role based
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // Privilege based
                        .requestMatchers(HttpMethod.DELETE, "/users/**")
                        .hasAuthority("USER_DELETE")

                        .requestMatchers(HttpMethod.GET, "/users/**")
                        .hasAuthority("USER_READ")

                        // Mixed
                        .requestMatchers("/appointments/create")
                        .hasAnyAuthority("APPOINTMENT_CREATE", "ROLE_ADMIN")

                        .anyRequest().authenticated()
                );

        return http.build();
    }


}

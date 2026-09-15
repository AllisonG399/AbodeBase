package com.abodebase.api.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ============================================
    // Security Filter Chain
    // ============================================

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        PasswordEncoder passwordEncoder
    ) throws Exception {

        http
            .authorizeHttpRequests(authorize -> authorize

                // Public endpoints
                .requestMatchers(
                    "/api/auth/**",
                    "/actuator/health"
                ).permitAll()

                // Admin endpoints
                .requestMatchers("/api/admin/**")
                .hasAuthority("ADMIN")

                // All other API endpoints require authentication
                .requestMatchers("/api/**")
                .authenticated()

                // Everything else
                .anyRequest().permitAll()
            )

            // Username/password authentication
            .formLogin(form -> form
                .permitAll()
            )

            // Logout
            .logout(logout -> logout
                .permitAll()
            )

            // Session-based authentication
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }
}
package com.abodebase.api.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import jakarta.servlet.http.HttpServletResponse;

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
                    "/api/auth/login",
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

            // Logout
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(
                    (request, response, authentication) ->
                        response.setStatus(HttpServletResponse.SC_OK)
                )
                .permitAll()
            )

            // API authentication errors
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            )

            // Session-based authentication
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }

    // ============================================
    // Authentication Manager
    // ============================================

    @Bean
    public AuthenticationManager authenticationManager(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authenticationProvider =
            new DaoAuthenticationProvider(userDetailsService);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    // ============================================
    // Security Context Repository
    // ============================================

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
    
}
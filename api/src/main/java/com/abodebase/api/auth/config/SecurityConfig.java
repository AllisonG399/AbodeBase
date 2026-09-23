package com.abodebase.api.auth.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import jakarta.servlet.ServletException;
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
        PasswordEncoder passwordEncoder,
        ConcurrentSessionFilter concurrentSessionFilter
    ) throws Exception {

        http
            .authorizeHttpRequests(authorize -> authorize

                // Public endpoints
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
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

            // Check whether an existing session has been expired by the SessionRegistry.
            http.addFilterBefore(
                concurrentSessionFilter,
                AuthorizationFilter.class
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

    // ============================================
    // Session Registry
    // ============================================

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // ============================================
    // Concurrent Session Filter
    // ============================================

    @Bean
    public ConcurrentSessionFilter concurrentSessionFilter(
        SessionRegistry sessionRegistry
    ) {

        SessionInformationExpiredStrategy expiredStrategy =
            new SessionInformationExpiredStrategy() {

                @Override
                public void onExpiredSessionDetected(
                    SessionInformationExpiredEvent event
                ) throws IOException, ServletException {

                    event.getResponse().setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                    );
                }
            };

        return new ConcurrentSessionFilter(
            sessionRegistry,
            expiredStrategy
        );
    }
    
}
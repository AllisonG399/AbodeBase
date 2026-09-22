package com.abodebase.api.auth.controller;

import com.abodebase.api.auth.dto.CurrentUserResponse;
import com.abodebase.api.auth.dto.LoginRequest;
import com.abodebase.api.auth.dto.RegisterRequest;
import com.abodebase.api.auth.service.RegistrationService;
import com.abodebase.api.auth.service.LoginAttemptService;
import com.abodebase.api.auth.service.RegistrationRateLimitService;
import com.abodebase.api.auth.exception.LoginRateLimitException;
import com.abodebase.api.auth.exception.RegistrationRateLimitException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.stream.Collectors;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final RegistrationService registrationService;
    private final LoginAttemptService loginAttemptService;
    private final RegistrationRateLimitService registrationRateLimitService;

    // ============================================
    // Constructor
    // ============================================

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            RegistrationService registrationService,
            LoginAttemptService loginAttemptService,
            RegistrationRateLimitService registrationRateLimitService
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.registrationService = registrationService;
        this.loginAttemptService = loginAttemptService;
        this.registrationRateLimitService = registrationRateLimitService;
    }


    // ============================================
    // Register
    // ============================================
    @PostMapping("/register")
    public ResponseEntity<Void> register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletRequest httpRequest
    ) {

        String ipAddress =
            httpRequest.getRemoteAddr();

        if (registrationRateLimitService.isBlocked(ipAddress)) {
            throw new RegistrationRateLimitException(
                "Too many registration attempts. Please try again later."
            );
        }

        registrationRateLimitService.recordRegistrationAttempt(
            ipAddress
        );

        registrationService.register(request);

        return ResponseEntity.status(201).build();
    }


    // ============================================
    // Login
    // ============================================

    @PostMapping("/login")
    public ResponseEntity<Void> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {

        String identifier = request.getIdentifier().trim().toLowerCase();
        String loginKey = identifier + ":" + httpRequest.getRemoteAddr();

        // Check whether this identifier/IP combination has reached the login attempt limit.
        if (loginAttemptService.isBlocked(loginKey)) {
            throw new LoginRateLimitException(
                "Too many login attempts. Please try again later."
            );
        }

        try {

            Authentication authentication =
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        identifier,
                        request.getPassword()
                    )
                );

            // Successful login clears previous failed attempts.
            loginAttemptService.resetAttempts(loginKey);

            var securityContext =
                org.springframework.security.core.context.SecurityContextHolder
                    .createEmptyContext();

            securityContext.setAuthentication(authentication);

            org.springframework.security.core.context.SecurityContextHolder
                .setContext(securityContext);

            securityContextRepository.saveContext(
                securityContext,
                httpRequest,
                httpResponse
            );

            return ResponseEntity.ok().build();

        } catch (AuthenticationException exception) {

            // Authentication failed, so record the attempt
            loginAttemptService.recordFailedAttempt(loginKey);

            throw exception;
        }
    }

    // ============================================
    // Current User
    // ============================================

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {

        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        Set<String> roles = authentication.getAuthorities()
            .stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toSet());

        CurrentUserResponse response =
            new CurrentUserResponse(
                authentication.getName(),
                roles
            );

        return ResponseEntity.ok(response);
    }
}
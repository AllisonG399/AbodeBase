package com.abodebase.api.auth.controller;

import com.abodebase.api.auth.dto.CurrentUserResponse;
import com.abodebase.api.auth.dto.LoginRequest;
import com.abodebase.api.auth.dto.RegisterRequest;
import com.abodebase.api.auth.service.RegistrationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.stream.Collectors;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final RegistrationService registrationService;


    // ============================================
    // Constructor
    // ============================================

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            RegistrationService registrationService
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.registrationService = registrationService;
    }


    // ============================================
    // Register
    // ============================================
    @PostMapping("/register")
    public ResponseEntity<Void> register(
        @Valid @RequestBody RegisterRequest request
    ) {
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

        Authentication authentication =
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getIdentifier().trim().toLowerCase(),
                    request.getPassword()
                )
            );

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
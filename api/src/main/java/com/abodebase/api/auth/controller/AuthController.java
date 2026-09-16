package com.abodebase.api.auth.controller;

import com.abodebase.api.auth.dto.CurrentUserResponse;
import com.abodebase.api.auth.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;


    // ============================================
    // Constructor
    // ============================================

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
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
                    request.getEmail().trim().toLowerCase(),
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
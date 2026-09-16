package com.abodebase.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    // ============================================
    // Fields
    // ============================================

    @NotBlank(message = "Username or email is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;


    // ============================================
    // Getters and Setters
    // ============================================

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
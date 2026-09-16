package com.abodebase.api.auth.dto;

import java.util.Set;

public class CurrentUserResponse {

    // ============================================
    // Fields
    // ============================================

    private String email;
    private Set<String> roles;


    // ============================================
    // Constructor
    // ============================================

    public CurrentUserResponse(String email, Set<String> roles) {
        this.email = email;
        this.roles = roles;
    }


    // ============================================
    // Getters
    // ============================================

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }
}

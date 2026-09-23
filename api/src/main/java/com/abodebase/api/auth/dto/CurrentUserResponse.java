package com.abodebase.api.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class CurrentUserResponse {

    // ============================================
    // Fields
    // ============================================

    private String username;
    private String email;
    private Set<String> roles;
    private LocalDateTime deletionRequestedAt;


    // ============================================
    // Constructor
    // ============================================

    public CurrentUserResponse(
        String username,
        String email, 
        Set<String> roles,
        LocalDateTime deletionRequestedAt
    ) {
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.deletionRequestedAt = deletionRequestedAt;
    }


    // ============================================
    // Getters
    // ============================================
    
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public LocalDateTime getDeletionRequestedAt() {
        return deletionRequestedAt;
    }
}

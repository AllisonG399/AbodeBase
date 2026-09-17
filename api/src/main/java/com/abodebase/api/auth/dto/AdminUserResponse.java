package com.abodebase.api.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class AdminUserResponse {

    private UUID id;
    private String username;
    private String email;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminUserResponse(
        UUID id,
        String username,
        String email,
        boolean enabled,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

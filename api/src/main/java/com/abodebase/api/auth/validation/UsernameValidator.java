package com.abodebase.api.auth.validation;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UsernameValidator {

    private static final Set<String> RESERVED_USERNAMES = Set.of(
        "admin",
        "administrator",
        "root",
        "system",
        "support",
        "api",
        "null",
        "undefined",
        "abodebase"
    );

    public String validateAndNormalize(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                "Username is required."
            );
        }

        String normalizedUsername = username.trim().toLowerCase();

        if (!normalizedUsername.matches("^[a-z0-9]{3,30}$")) {
            throw new IllegalArgumentException(
                "Username must be 3-30 characters and contain only letters and numbers."
            );
        }

        if (RESERVED_USERNAMES.contains(normalizedUsername)) {
            throw new IllegalArgumentException(
                "This username cannot be used."
            );
        }

        return normalizedUsername;
    }
}
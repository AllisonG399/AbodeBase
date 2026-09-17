package com.abodebase.api.auth.validation;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final Set<String> blockedUsernames;

    public UsernameValidator() {
        this.blockedUsernames = loadBlockedUsernames();
    }

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

        if (blockedUsernames.contains(normalizedUsername)) {
            throw new IllegalArgumentException(
                "This username cannot be used."
            );
        }

        return normalizedUsername;
    }

    private Set<String> loadBlockedUsernames() {

        try (InputStream inputStream =
                 new ClassPathResource("username-blocklist.txt").getInputStream()) {

            return new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
            )
                .lines()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("#"))
                .collect(Collectors.toSet());

        } catch (IOException exception) {

            throw new IllegalStateException(
                "Unable to load username blocklist.",
                exception
            );
        }
    }
}
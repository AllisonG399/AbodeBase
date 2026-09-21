package com.abodebase.api.auth.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;

    private static final Duration ATTEMPT_WINDOW =
        Duration.ofMinutes(15);

    private final Map<String, AttemptRecord> attempts =
        new ConcurrentHashMap<>();

    // ============================================
    // Check Login Attempts
    // ============================================

    public boolean isBlocked(String key) {

        AttemptRecord record =
            attempts.get(key);

        if (record == null) {
            return false;
        }

        if (record.isExpired()) {
            attempts.remove(key);
            return false;
        }

        return record.failedAttempts >= MAX_ATTEMPTS;
    }

    // ============================================
    // Record Failed Login
    // ============================================

    public void recordFailedAttempt(String key) {

        attempts.compute(
            key,
            (existingKey, existingRecord) -> {

                if (
                    existingRecord == null ||
                    existingRecord.isExpired()
                ) {
                    return new AttemptRecord();
                }

                existingRecord.failedAttempts++;

                return existingRecord;
            }
        );
    }

    // ============================================
    // Reset Login Attempts
    // ============================================

    public void resetAttempts(String key) {
        attempts.remove(key);
    }

    // ============================================
    // Attempt Record
    // ============================================

    private static class AttemptRecord {

        private int failedAttempts = 1;

        private final Instant firstAttempt =
            Instant.now();

        private boolean isExpired() {

            return Instant.now().isAfter(
                firstAttempt.plus(ATTEMPT_WINDOW)
            );
        }
    }
}

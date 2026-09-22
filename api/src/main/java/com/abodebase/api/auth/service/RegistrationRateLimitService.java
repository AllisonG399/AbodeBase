package com.abodebase.api.auth.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationRateLimitService {

    private static final int MAX_ATTEMPTS = 5;

    private static final Duration ATTEMPT_WINDOW =
        Duration.ofMinutes(15);

    private final Map<String, AttemptRecord> attempts =
        new ConcurrentHashMap<>();

    public void resetAllAttempts() {
        attempts.clear();
    }

    public boolean isBlocked(String ipAddress) {

        AttemptRecord record =
            attempts.get(ipAddress);

        if (record == null) {
            return false;
        }

        if (record.isExpired()) {
            attempts.remove(ipAddress);
            return false;
        }

        return record.registrationAttempts >= MAX_ATTEMPTS;
    }

    public void recordRegistrationAttempt(String ipAddress) {

        attempts.compute(
            ipAddress,
            (existingKey, existingRecord) -> {

                if (
                    existingRecord == null ||
                    existingRecord.isExpired()
                ) {
                    return new AttemptRecord();
                }

                existingRecord.registrationAttempts++;

                return existingRecord;
            }
        );
    }

    private static class AttemptRecord {

        private int registrationAttempts = 1;

        private final Instant firstAttempt =
            Instant.now();

        private boolean isExpired() {

            return Instant.now().isAfter(
                firstAttempt.plus(ATTEMPT_WINDOW)
            );
        }
    }
}

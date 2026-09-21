package com.abodebase.api.auth.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Include:
 * New Identifier is Not Blocked --> A new login key starts clean
 * One Failed Attempt is Not Blocked --> One failure is allowed
 * Four Failed Attempts are Not Blocked --> The user still has one attempt remaining
 * Five Failed Attempts are Blocked --> The fifth fialure triggers the limit
 * Reset Attempts Removes Block --> A successful login clears the failed-attempt counter
 */

public class LoginAttemptServiceTest {
    
    // ============================================
    // New Identifier is Not Blocked
    // ============================================
    @Test
    void newIdentifierIsNotBlocked() {

        LoginAttemptService service = new LoginAttemptService();

        assertFalse(service.isBlocked("user@example.com:127.0.0.1"));
    }

    // ============================================
    // One Failed Attempt is Not Blocked
    // ============================================
    @Test
    void oneFailedAttemptIsNotBlocked() {

        LoginAttemptService service = new LoginAttemptService();

        String key = "user@example.com:127.0.0.1";

        service.recordFailedAttempt(key);

        assertFalse(service.isBlocked(key));
    }

    // ============================================
    // Four Failed Attempts are Not Blocked
    // ============================================
    @Test
    void fourFailedAttemptsAreNotBlocked() {

        LoginAttemptService service = new LoginAttemptService();

        String key = "user@example.com:127.0.0.1";

        for (int i = 1; i < 5; i++) {
            service.recordFailedAttempt(key);
        }

        assertFalse(service.isBlocked(key));
    }

    // ============================================
    // Five Failed Attempts are Blocked
    // ============================================
    @Test
    void fiveFailedAttemptsAreBlocked() {

        LoginAttemptService service = new LoginAttemptService();

        String key = "user@example.com:127.0.0.1";

        for (int i = 1; i < 6; i++) {
            service.recordFailedAttempt(key);
        }

        assertTrue(service.isBlocked(key));
    }

    // ============================================
    // Reset Attempts Removed Block
    // ============================================
    @Test
    void resetAttemptsRemovesBlock() {

        LoginAttemptService service = new LoginAttemptService();

        String key = "user@example.com:127.0.0.1";

        for (int i = 1; i < 6; i++) {
            service.recordFailedAttempt(key);
        }

        assertTrue(service.isBlocked(key));

        // Successful login resets the attempts
        service.resetAttempts(key);

        assertFalse(service.isBlocked(key));
    }
}

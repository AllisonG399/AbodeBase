package com.abodebase.api.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Include:
 * New IP --> A new IP can register
 * Four Registration Attempts --> The limiter does not block too early
 * Five Registration Attempts --> The fifth attempt receives the block
 * Different IP Addresses     --> One IP being blocked does not affect another IP
 */
class RegistrationRateLimitServiceTest {

    // ============================================
    // New IP
    // ============================================

    @Test
    void newIpIsNotBlocked() {

        RegistrationRateLimitService service =
            new RegistrationRateLimitService();

        assertFalse(
            service.isBlocked("192.168.1.100")
        );
    }

    // ============================================
    // Four Registration Attempts
    // ============================================

    @Test
    void fourRegistrationAttemptsAreAllowed() {

        RegistrationRateLimitService service =
            new RegistrationRateLimitService();

        String ipAddress =
            "192.168.1.100";

        service.recordRegistrationAttempt(ipAddress);
        service.recordRegistrationAttempt(ipAddress);
        service.recordRegistrationAttempt(ipAddress);
        service.recordRegistrationAttempt(ipAddress);

        assertFalse(
            service.isBlocked(ipAddress)
        );
    }

    // ============================================
    // Five Registration Attempts is Blocked
    // ============================================

    @Test
    void fifthRegistrationAttemptIsBlocked() {

        RegistrationRateLimitService service =
            new RegistrationRateLimitService();

        String ipAddress =
            "192.168.1.100";

        service.recordRegistrationAttempt(ipAddress);
        service.recordRegistrationAttempt(ipAddress);
        service.recordRegistrationAttempt(ipAddress);
        service.recordRegistrationAttempt(ipAddress);
        service.recordRegistrationAttempt(ipAddress);

        assertTrue(
            service.isBlocked(ipAddress)
        );
    }


    // ============================================
    // Different IP Addresses
    // ============================================

    @Test
    void differentIpAddressesHaveSeparateLimits() {

        RegistrationRateLimitService service =
            new RegistrationRateLimitService();

        String firstIp =
            "192.168.1.100";

        String secondIp =
            "192.168.1.101";

        // Block the first IP.
        for (int i = 0; i < 6; i++) {
            service.recordRegistrationAttempt(firstIp);
        }

        // Second IP has its own limit.
        assertTrue(
            service.isBlocked(firstIp)
        );

        assertFalse(
            service.isBlocked(secondIp)
        );
    }
}

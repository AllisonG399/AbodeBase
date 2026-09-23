package com.abodebase.api.auth.service;

import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.repository.UserRepository;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Include:
 * Expired Account is Permanently Deleted
 * Recent Account is Not Deleted
 * Active Account is Not Deleted
 */
@SpringBootTest
class UserDeletionCleanupServiceTest {

    @Autowired
    private UserDeletionCleanupService cleanupService;

    @Autowired
    private UserRepository userRepository;


    // ============================================
    // Expired Account Is Permanently Deleted
    // ============================================

    @Test
    void expiredAccountIsPermanentlyDeleted() {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        User user = new User();

        user.setUsername(
            "expired" + timestamp
        );

        user.setEmail(
            "expired" + timestamp + "@example.com"
        );

        user.setPasswordHash(
            "test-password"
        );

        user.setEnabled(true);

        user.setDeletionRequestedAt(
            LocalDateTime.now().minusDays(31)
        );

        User savedUser =
            userRepository.save(user);

        cleanupService.deleteExpiredAccounts();

        assertTrue(
            userRepository.findById(savedUser.getId()).isEmpty()
        );
    }


    // ============================================
    // Recent Account Is Not Deleted
    // ============================================

    @Test
    void recentAccountIsNotDeleted() {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        User user = new User();

        user.setUsername(
            "recent" + timestamp
        );

        user.setEmail(
            "recent" + timestamp + "@example.com"
        );

        user.setPasswordHash(
            "test-password"
        );

        user.setEnabled(true);

        user.setDeletionRequestedAt(
            LocalDateTime.now().minusDays(29)
        );

        User savedUser =
            userRepository.save(user);

        cleanupService.deleteExpiredAccounts();

        assertTrue(
            userRepository.findById(savedUser.getId()).isPresent()
        );

        // Clean up test data
        userRepository.delete(savedUser);
    }


    // ============================================
    // Active Account Is Not Deleted
    // ============================================

    @Test
    void activeAccountIsNotDeleted() {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        User user = new User();

        user.setUsername(
            "active" + timestamp
        );

        user.setEmail(
            "active" + timestamp + "@example.com"
        );

        user.setPasswordHash(
            "test-password"
        );

        user.setEnabled(true);

        user.setDeletionRequestedAt(null);

        User savedUser =
            userRepository.save(user);

        cleanupService.deleteExpiredAccounts();

        assertTrue(
            userRepository.findById(savedUser.getId()).isPresent()
        );

        // Clean up test data
        userRepository.delete(savedUser);
    }

}

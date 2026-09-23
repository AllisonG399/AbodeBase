package com.abodebase.api.auth.service;

import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDeletionCleanupService {

    private static final int DELETION_GRACE_PERIOD_DAYS = 30;

    private final UserRepository userRepository;

    public UserDeletionCleanupService(
        UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteExpiredAccounts() {

        LocalDateTime cutoff =
            LocalDateTime.now()
                .minusDays(DELETION_GRACE_PERIOD_DAYS);

        List<User> users =
            userRepository.findUsersPendingDeletionBefore(
                cutoff
            );

        userRepository.deleteAll(users);
    }
}

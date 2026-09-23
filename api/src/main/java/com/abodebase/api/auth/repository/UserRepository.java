package com.abodebase.api.auth.repository;

import com.abodebase.api.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCaseOrUsernameIgnoreCase(
        String email,
        String username
    );

    @Query("""
        SELECT u
        FROM User u
        WHERE u.deletionRequestedAt IS NOT NULL
        AND u.deletionRequestedAt <= :cutoff
    """)
    List<User> findUsersPendingDeletionBefore(
        @Param("cutoff") LocalDateTime cutoff
    );
}

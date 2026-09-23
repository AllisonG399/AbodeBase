package com.abodebase.api.auth.service;

import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get User by Id
    public User getUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "User not found."
            ));
    }

    // Get User by Email
    private User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found.")
            );
    }

    // Request Account Deletion - Hard deletes at 30 days
    public void requestAccountDeletion(
        String email,
        String password
    ) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(
            password,
            user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                "Invalid password."
            );
        }

        user.setDeletionRequestedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // Permanently Delete User
    public void permanentlyDeleteUser(
        String email,
        String password
    ) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(
            password,
            user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                "Invalid password."
            );
        }

        userRepository.delete(user);
    }

    // Recovery Method
    public void cancelAccountDeletion(String email) {
        User user = getUserByEmail(email);

        if (user.getDeletionRequestedAt() == null) {
            throw new IllegalArgumentException(
                "Account is not pending deletion."
            );
        }

        user.setDeletionRequestedAt(null);

        userRepository.save(user);
    }

    // Update Users Status
    public User updateUserStatus(UUID id, boolean enabled) {

        User user = getUserById(id);

        // Prevent the sole admin from disabling their own account.
        if (!enabled && hasAdminRole(user)) {
            throw new IllegalArgumentException(
                "The admin account cannot be disabled."
            );
        }

        user.setEnabled(enabled);

        return userRepository.save(user);
    }

    // Check Admin Role
    private boolean hasAdminRole(User user) {

        return user.getRoles()
            .stream()
            .anyMatch(role ->
                "ADMIN".equals(role.getName())
            );
    }

    // Get List of all Users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

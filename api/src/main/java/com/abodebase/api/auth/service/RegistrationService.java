package com.abodebase.api.auth.service;

import com.abodebase.api.auth.dto.RegisterRequest;
import com.abodebase.api.auth.entity.Role;
import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.repository.RoleRepository;
import com.abodebase.api.auth.repository.UserRepository;
import com.abodebase.api.auth.validation.UsernameValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsernameValidator usernameValidator;

    // ============================================
    // Constructor
    // ============================================

    public RegistrationService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        UsernameValidator usernameValidator
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.usernameValidator = usernameValidator;
    }

    // ============================================
    // Register User
    // ============================================

    public User register(RegisterRequest request) {

        String username =
            usernameValidator.validateAndNormalize(
                request.getUsername()
            );

        String email =
            request.getEmail().trim().toLowerCase();

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException(
                "Username is already taken."
            );
        }

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException(
                "Email is already registered."
            );
        }

        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new IllegalStateException(
                "USER role was not found in the database."
            ));

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(
            passwordEncoder.encode(request.getPassword())
        );
        user.setEnabled(true);
        user.getRoles().add(userRole);

        return userRepository.save(user);
    }
}

package com.abodebase.api.auth.config;

import com.abodebase.api.auth.entity.Role;
import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.repository.RoleRepository;
import com.abodebase.api.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${abodebase.admin.email:}")
    private String adminEmail;

    @Value("${abodebase.admin.password:}")
    private String adminPassword;


    // ============================================
    // Constructor
    // ============================================

    public AdminInitializer(
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // ============================================
    // Initialize Admin
    // ============================================

    @Override
    public void run(String... args) {

        // Do not create an account if one already exists.
        if (userRepository.count() > 0) {
            return;
        }

        // Make sure the required environment variables exist.
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException(
                "Admin credentials are not configured. " +
                "Set ABODEBASE_ADMIN_EMAIL and ABODEBASE_ADMIN_PASSWORD."
            );
        }

        // Find the ADMIN role created by Flyway.
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseThrow(() -> new IllegalStateException(
                "ADMIN role was not found in the database."
            ));

        // Create the initial admin account.
        User admin = new User();
        admin.setEmail(adminEmail.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setEnabled(true);
        admin.getRoles().add(adminRole);

        userRepository.save(admin);

        System.out.println("Initial admin account created successfully.");
    }
}
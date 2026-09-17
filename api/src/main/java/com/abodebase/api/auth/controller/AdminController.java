package com.abodebase.api.auth.controller;

import com.abodebase.api.auth.dto.AdminUserResponse;
import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ============================================
    // Get All Users
    // ============================================

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {

        List<AdminUserResponse> users =
            userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    // ============================================
    // Convert User Entity to Response
    // ============================================

    private AdminUserResponse toResponse(User user) {

        return new AdminUserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.isEnabled(),
            user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

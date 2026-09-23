package com.abodebase.api.auth.controller;

import com.abodebase.api.auth.dto.AdminUserResponse;
import com.abodebase.api.auth.dto.UpdateUserStatusRequest;
import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.service.UserService;
import com.abodebase.api.auth.service.UserSessionService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final UserSessionService userSessionService;

    public AdminController(
        UserService userService,
        UserSessionService userSessionService
    ) {
        this.userService = userService;
        this.userSessionService = userSessionService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {

        List<AdminUserResponse> users =
            userService.getAllUsers()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(
        @PathVariable UUID id
    ) {

        User user = userService.getUserById(id);

        return ResponseEntity.ok(
            toResponse(user)
        );
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserStatusRequest request
    ) {

        User user = userService.updateUserStatus(
            id,
            request.getEnabled()
        );

        if (!request.getEnabled()) {
            userSessionService.expireUserSessions(
                user.getEmail()
            );
        }

        return ResponseEntity.ok(
            toResponse(user)
        );
    }

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
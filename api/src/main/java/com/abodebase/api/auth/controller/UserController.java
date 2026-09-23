package com.abodebase.api.auth.controller;

import com.abodebase.api.auth.dto.DeleteAccountRequest;
import com.abodebase.api.auth.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ============================================
    // Delete Current User - Soft Delete
    // ============================================

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(
        @Valid @RequestBody DeleteAccountRequest deleteRequest,
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        userService.requestAccountDeletion(
            authentication.getName(),
            deleteRequest.getPassword()
        );

        SecurityContextLogoutHandler logoutHandler =
            new SecurityContextLogoutHandler();

        logoutHandler.logout(
            request,
            response,
            authentication
        );

        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Recovery Method
    // ============================================

    @PostMapping("/me/cancel-deletion")
    public ResponseEntity<Void> cancelAccountDeletion(
        Authentication authentication
    ) {
        userService.cancelAccountDeletion(
            authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Delete Current User - Hard Delete
    // ============================================

    @DeleteMapping("/me/permanent")
    public ResponseEntity<Void> permanentlyDeleteCurrentUser(
        @Valid @RequestBody DeleteAccountRequest deleteRequest,
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        userService.permanentlyDeleteUser(
            authentication.getName(),
            deleteRequest.getPassword()
        );

        SecurityContextLogoutHandler logoutHandler =
            new SecurityContextLogoutHandler();

        logoutHandler.logout(
            request,
            response,
            authentication
        );

        return ResponseEntity.noContent().build();
    }

}
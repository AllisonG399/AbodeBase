package com.abodebase.api.auth.controller;

import com.abodebase.api.auth.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    // Delete Current User
    // ============================================

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response
    ) {

        userService.deleteUser(authentication.getName());

        SecurityContextLogoutHandler logoutHandler =
            new SecurityContextLogoutHandler();

        logoutHandler.logout(request, response, authentication);

        return ResponseEntity.noContent().build();
    }
}
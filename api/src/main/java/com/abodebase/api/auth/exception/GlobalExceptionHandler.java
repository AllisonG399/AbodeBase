package com.abodebase.api.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================
    // Illegal Argument
    // ============================================

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalArgumentException(
        IllegalArgumentException exception
    ) {
        // The exception is converted into a 400 Bad Request response.
    }

    // ============================================
    // Login Rate Limit
    // ============================================

    @ExceptionHandler(LoginRateLimitException.class)
    public ResponseEntity<String> handleLoginRateLimit(
        LoginRateLimitException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(exception.getMessage());
    }

    // ============================================
    // Registration Rate Limit
    // ============================================

    @ExceptionHandler(RegistrationRateLimitException.class)
    public ResponseEntity<String> handleRegistrationRateLimit(
        RegistrationRateLimitException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(exception.getMessage());
    }
}

package com.abodebase.api.auth.exception;

public class LoginRateLimitException extends RuntimeException {
    
    public LoginRateLimitException(String message) {
        super(message);
    }
}

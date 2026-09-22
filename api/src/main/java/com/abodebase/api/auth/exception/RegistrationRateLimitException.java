package com.abodebase.api.auth.exception;

public class RegistrationRateLimitException extends RuntimeException {

    public RegistrationRateLimitException(String message) {
        super(message);
    }
}

package org.example.userservice.exception.authException;

public class VerificationEmailFailureException extends RuntimeException {
    public VerificationEmailFailureException(String message) {
        super(message);
    }
}

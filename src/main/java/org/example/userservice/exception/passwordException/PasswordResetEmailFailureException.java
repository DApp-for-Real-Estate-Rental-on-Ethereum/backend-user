package org.example.userservice.exception.passwordException;

public class PasswordResetEmailFailureException extends RuntimeException {
    public PasswordResetEmailFailureException(String message) {
        super(message);
    }
}

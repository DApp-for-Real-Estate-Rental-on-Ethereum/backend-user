package org.example.userservice.exception.passwordException;

public class UsedPasswordResetTokenException extends RuntimeException {
    public UsedPasswordResetTokenException(String message) {
        super(message);
    }
}

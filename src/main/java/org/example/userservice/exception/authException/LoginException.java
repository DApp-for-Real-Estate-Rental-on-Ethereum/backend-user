package org.example.userservice.exception.authException;

public class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}

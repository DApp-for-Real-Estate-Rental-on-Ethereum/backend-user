package org.example.userservice.exception.authException;

public class WrongVerificationCodeException extends RuntimeException {
    public WrongVerificationCodeException(String message) {
        super(message);
    }
}

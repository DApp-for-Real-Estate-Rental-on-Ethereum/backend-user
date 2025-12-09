package org.example.userservice.exception.userException;

public class UnderRequiredAgeException extends RuntimeException {
    public UnderRequiredAgeException(String message) {
        super(message);
    }
}

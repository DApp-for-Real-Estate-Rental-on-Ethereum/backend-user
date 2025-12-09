package org.example.userservice.exception.userException;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {super(message);}
}
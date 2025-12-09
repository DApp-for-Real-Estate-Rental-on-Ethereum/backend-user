package org.example.userservice.service;

import org.example.userservice.exception.userException.UnderRequiredAgeException;
import org.example.userservice.exception.userException.UserAlreadyExistsException;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class UserValidationService {
    private final UserRepository userRepository;

    public UserValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateIsAdult(LocalDate birthday) {
        int age = Period.between(birthday, LocalDate.now()).getYears();
        if (age < 18) {
            throw new UnderRequiredAgeException("User must be at least 18");
        }
    }
}
package org.example.userservice.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
public class UpdateUserRequestDTO {

    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;

    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;

    @Pattern(regexp = "\\d{10,15}", message = "Phone number must be numeric and between 10 and 15 digits")
    private String phoneNumber;

    private String walletAddress;
}

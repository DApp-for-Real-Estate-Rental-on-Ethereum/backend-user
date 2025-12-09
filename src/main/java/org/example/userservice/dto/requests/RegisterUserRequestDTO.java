package org.example.userservice.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.example.userservice.enums.UserRoleEnum;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class RegisterUserRequestDTO implements Serializable {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,20}$",
            message = "Password must be 8-20 characters long, include at least one uppercase letter, " +
                    "one lowercase letter, one number, and one special character"
    )
    private String password;

    @NotNull(message = "Birthday is required")
    private LocalDate birthday;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10,15}", message = "Phone number must be 10 to 15 digits")
    private String phoneNumber;

    private UserRoleEnum role;
}

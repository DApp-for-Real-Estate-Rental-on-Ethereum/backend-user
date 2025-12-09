package org.example.userservice.service.serviceInterfaces;

import org.example.userservice.dto.requests.LoginUserRequestDTO;
import org.example.userservice.dto.requests.RegisterUserRequestDTO;
import org.example.userservice.dto.requests.ResetPasswordRequestDTO;
import org.example.userservice.dto.requests.VerifyUserRequestDTO;
import org.example.userservice.dto.responses.LoginUserResponseDTO;

public interface IAuthenticationService {

    void signup(RegisterUserRequestDTO input);

    LoginUserResponseDTO login(LoginUserRequestDTO input);

    void verifyUser(VerifyUserRequestDTO input);

    void resendVerificationCode(String email);

    void sendPasswordResetEmail(String email);

    void validateResetPasswordToken(String token);

    void changePassword(ResetPasswordRequestDTO input);
}
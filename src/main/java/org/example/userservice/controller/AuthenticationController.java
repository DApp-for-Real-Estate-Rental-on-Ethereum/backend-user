package org.example.userservice.controller;

import jakarta.validation.Valid;
import org.example.userservice.dto.requests.LoginUserRequestDTO;
import org.example.userservice.dto.requests.RegisterUserRequestDTO;
import org.example.userservice.dto.requests.ResetPasswordRequestDTO;
import org.example.userservice.dto.requests.VerifyUserRequestDTO;
import org.example.userservice.dto.responses.LoginUserResponseDTO;
import org.example.userservice.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody
            @Valid
            RegisterUserRequestDTO registerUserDto
    ) {
        authenticationService.signup(registerUserDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponseDTO> authenticate(
            @Valid
            @RequestBody LoginUserRequestDTO loginUserDto
    ) {
        LoginUserResponseDTO responseDTO = authenticationService.login(loginUserDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(
            @Valid
            @RequestBody VerifyUserRequestDTO verifyUserDto
    ) {
        authenticationService.verifyUser(verifyUserDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User verified successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(
            @RequestParam String email
    ) {
        authenticationService.resendVerificationCode(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification Code resent successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @RequestBody Map<String, Object> email
    ) {
        authenticationService.sendPasswordResetEmail(email.get("email").toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-reset-token")
    public ResponseEntity<Map<String, Boolean>> validateResetPassword(
            @RequestBody Map<String,Object> token
    )
    {
        authenticationService.validateResetPasswordToken(token.get("token").toString());
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody
            @Valid
            ResetPasswordRequestDTO resetPasswordDto
    ) {
        authenticationService.changePassword(resetPasswordDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, Boolean>> verifyResetCode(
            @RequestBody Map<String, Object> request
    ) {
        String email = request.get("email").toString();
        String code = request.get("code").toString();
        authenticationService.validateResetPasswordCode(email, code);
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @PostMapping("/reset-password-with-code")
    public ResponseEntity<Map<String, String>> resetPasswordWithCode(
            @RequestBody Map<String, Object> request
    ) {
        String email = request.get("email").toString();
        String code = request.get("code").toString();
        String newPassword = request.get("newPassword").toString();
        authenticationService.resetPasswordWithCode(email, code, newPassword);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        return ResponseEntity.ok(response);
    }
}

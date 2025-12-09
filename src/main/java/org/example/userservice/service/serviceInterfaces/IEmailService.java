package org.example.userservice.service.serviceInterfaces;

import java.time.LocalDateTime;

public interface IEmailService {

    public void sendVerificationEmail(Long userId, String userEmail, String fullName, String verificationCode, LocalDateTime verificationCodeExpiresAt);

    public void sendWelcomeEmail(Long userId, String fullName, String email);

    void sendPasswordResetEmail(Long userId, String email, String fullName, String token, String expiresAt);
}

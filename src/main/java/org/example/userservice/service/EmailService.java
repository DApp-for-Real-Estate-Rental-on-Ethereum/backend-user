package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.config.NotificationProducer;
import org.example.userservice.enums.ChannelTypeEnum;
import org.example.userservice.service.serviceInterfaces.IEmailService;
import org.example.userservice.dto.requests.NotificationRequestDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final NotificationProducer notificationProducer;

    @Override
    public void sendPasswordResetEmail(Long userId, String userEmail, String fullName, String token, String expiresAt){
        NotificationRequestDTO notificationRequestDTO = new NotificationRequestDTO();

        notificationRequestDTO.setUserId(userId != null ? userId.toString() : null);
        notificationRequestDTO.setCreatedAt(LocalDateTime.now());
        notificationRequestDTO.setChannel(ChannelTypeEnum.PASSWORD_RESET_EMAIL);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userEmail", userEmail);
        data.put("expiresAt", expiresAt);
        data.put("fullName", fullName);

        notificationRequestDTO.setMessage(data);

        notificationProducer.sendNotification(notificationRequestDTO);
    };

    @Override
    public void sendVerificationEmail(Long userId,String userEmail, String fullName, String verificationCode, LocalDateTime verificationCodeExpiresAt) {
        NotificationRequestDTO notificationRequestDTO = new NotificationRequestDTO();

        notificationRequestDTO.setUserId(userId != null ? userId.toString() : null);
        notificationRequestDTO.setCreatedAt(LocalDateTime.now());
        notificationRequestDTO.setChannel(ChannelTypeEnum.ACCOUNT_VERIFICATION_EMAIL);

        Map<String, Object> data = new HashMap<>();
        data.put("verificationCode", verificationCode);
        data.put("expiresAt", verificationCodeExpiresAt);
        data.put("fullName", fullName);
        data.put("userEmail", userEmail);

        notificationRequestDTO.setMessage(data);

        notificationProducer.sendNotification(notificationRequestDTO);
    }

    @Override
    public void sendWelcomeEmail(Long userId, String fullName, String userEmail) {
        NotificationRequestDTO notificationRequestDTO = new NotificationRequestDTO();

        notificationRequestDTO.setUserId(userId != null ? userId.toString() : null);
        notificationRequestDTO.setCreatedAt(LocalDateTime.now());
        notificationRequestDTO.setChannel(ChannelTypeEnum.WELCOME_EMAIL);
        Map<String, Object> data = new HashMap<>();
        data.put("userEmail", userEmail);
        data.put("fullName", fullName);
        notificationRequestDTO.setMessage(data);
        notificationProducer.sendNotification(notificationRequestDTO);
    }
}

package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.requests.ChangePasswordRequestDTO;
import org.example.userservice.dto.requests.LoginUserRequestDTO;
import org.example.userservice.dto.requests.RegisterUserRequestDTO;
import org.example.userservice.dto.requests.ResetPasswordRequestDTO;
import org.example.userservice.dto.requests.VerifyUserRequestDTO;
import org.example.userservice.dto.responses.LoginUserResponseDTO;
import org.example.userservice.enums.UserRoleEnum;
import org.example.userservice.exception.authException.*;
import org.example.userservice.exception.passwordException.PasswordResetTokenNotFoundException;
import org.example.userservice.exception.passwordException.UsedPasswordResetTokenException;
import org.example.userservice.exception.passwordException.WrongPasswordException;
import org.example.userservice.exception.userException.*;
import org.example.userservice.model.PasswordResetToken;
import org.example.userservice.model.User;
import org.example.userservice.repository.PasswordResetTokenRepository;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.serviceInterfaces.IAuthenticationService;
import org.example.userservice.service.serviceInterfaces.IEmailService;
import org.example.userservice.util.HashingUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    @Value("${app.security.verification-code.expiry-minutes}")
    private long verificationCodeExpiryMinutes;

    @Value("${app.security.password-reset.expiry-minutes}")
    private long passwordResetExpiryMinutes;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final IEmailService emailService;
    private final JWTService jWTService;
    private final HashingUtil hashingUtil;

    @Transactional
    public void signup(RegisterUserRequestDTO input) {
        validateUserForRegistering(input);

        User user = buildNewUser(input);
        
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of(UserRoleEnum.TENANT));
        }

        User savedUser = userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getEmail()
            );
        } catch (Exception e) {
        }

        try {
            emailService.sendVerificationEmail(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    savedUser.getVerificationCode(),
                    savedUser.getVerificationCodeExpiresAt()
            );
        } catch (Exception e) {
        }
    }

    public LoginUserResponseDTO login(LoginUserRequestDTO input) {
        User user = userRepository.findByEmail(input.getEmail()).orElseThrow(
                () -> new UserNotFoundException("User Not Found!")
        );

        if (!checkHashPassword(user.getPassword(), input.getPassword())) {
            throw new WrongPasswordException("Wrong password!");
        }

        if (!user.isEnabled()) {
            throw new DisabledAccountException("Account Disabled!");
        }

        HashMap<String, Object> roleClaim = new HashMap<>();
        roleClaim.put("roles", user.getRoles());
        String token = jWTService.generateToken(roleClaim, user);

        return new LoginUserResponseDTO(token, jWTService.getExpirationTime());
    }

    @Transactional
    public void verifyUser(VerifyUserRequestDTO input) {
        User user = findUserByEmail(input.getEmail());

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredVerificationCodeException("Expired verification code!");
        }

        if (!user.getVerificationCode().equals(input.getVerificationCode())) {
            throw new WrongVerificationCodeException("Wrong verification code!");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerificationCode(String email) {

        User user = findUserByEmail(email);

        if (user.isEnabled()) {
            throw new AlreadyVerifiedException("User is already verified");
        }

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));

        emailService.sendVerificationEmail(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getVerificationCode(),
                user.getVerificationCodeExpiresAt());

        userRepository.save(user);

    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = findUserByEmail(email);
        String resetCode = generateVerificationCode();
        String hashedCode = hashToken(resetCode);

        PasswordResetToken passwordResetToken = PasswordResetToken
                .builder()
                .token(hashedCode)
                .user(user)
                .used(false)
                .valid(true)
                .expiresAt(LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes))
                .build();

        emailService.sendPasswordResetEmail(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                resetCode,
                passwordResetToken.getExpiresAt().toString());

        passwordResetTokenRepository.save(passwordResetToken);
    }

    public void validateResetPasswordToken(String token) {
        Optional<PasswordResetToken> optionalPasswordResetToken =
                passwordResetTokenRepository.findByToken(hashToken(token));

        if (optionalPasswordResetToken.isPresent()) {
            PasswordResetToken passwordResetToken = optionalPasswordResetToken.get();

            if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ExpiredPasswordResetTokenException("Token expired!");
            }

            if (!passwordResetToken.isValid()) {
                throw new InvalidPasswordResetTokenException("Invalid token!");
            }

            if (passwordResetToken.isUsed()) {
                throw new UsedPasswordResetTokenException("Token used!");
            }
        } else {
            throw new PasswordResetTokenNotFoundException("Token Not Found!");
        }
    }

    public void validateResetPasswordCode(String email, String code) {
        User user = findUserByEmail(email);
        String hashedCode = hashToken(code);
        
        Optional<PasswordResetToken> optionalPasswordResetToken =
                passwordResetTokenRepository.findByToken(hashedCode);

        if (optionalPasswordResetToken.isPresent()) {
            PasswordResetToken passwordResetToken = optionalPasswordResetToken.get();

            if (!passwordResetToken.getUser().getId().equals(user.getId())) {
                throw new InvalidPasswordResetTokenException("Invalid reset code for this email!");
            }

            if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ExpiredPasswordResetTokenException("Reset code expired!");
            }

            if (!passwordResetToken.isValid()) {
                throw new InvalidPasswordResetTokenException("Invalid reset code!");
            }

            if (passwordResetToken.isUsed()) {
                throw new UsedPasswordResetTokenException("Reset code already used!");
            }
        } else {
            throw new PasswordResetTokenNotFoundException("Reset code not found!");
        }
    }

    @Transactional
    public void changePassword(ResetPasswordRequestDTO input) {
        Optional<PasswordResetToken> optionalPasswordResetToken =
                passwordResetTokenRepository
                        .findByToken(hashToken(input.getToken()));

        if (optionalPasswordResetToken.isPresent()) {
            PasswordResetToken passwordResetToken = optionalPasswordResetToken.get();

            if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ExpiredPasswordResetTokenException("Token expired!");
            }

            if (passwordResetToken.isUsed()) {
                throw new UsedPasswordResetTokenException("Token used!");
            }

            User user = passwordResetToken.getUser();
            user.setPassword(hashPassword(input.getPassword()));
            userRepository.save(user);

            passwordResetToken.setUsed(true);
            passwordResetTokenRepository.save(passwordResetToken);

            invalidateOldTokens(user);
        } else {
            throw new PasswordResetTokenNotFoundException("Token Not Found!");
        }
    }

    @Transactional
    public void resetPasswordWithCode(String email, String code, String newPassword) {
        validateResetPasswordCode(email, code);
        
        String hashedCode = hashToken(code);
        Optional<PasswordResetToken> optionalPasswordResetToken =
                passwordResetTokenRepository.findByToken(hashedCode);

        if (optionalPasswordResetToken.isPresent()) {
            PasswordResetToken passwordResetToken = optionalPasswordResetToken.get();
            User user = passwordResetToken.getUser();

            if (!user.getEmail().equals(email)) {
                throw new InvalidPasswordResetTokenException("Email does not match reset code!");
            }

            user.setPassword(hashPassword(newPassword));
            userRepository.save(user);

            passwordResetToken.setUsed(true);
            passwordResetTokenRepository.save(passwordResetToken);

            invalidateOldTokens(user);
        } else {
            throw new PasswordResetTokenNotFoundException("Reset code not found!");
        }
    }

    private User buildNewUser(RegisterUserRequestDTO input) {
        return User.builder()
                .firstName(input.getFirstName())
                .lastName(input.getLastName())
                .email(input.getEmail())
                .phoneNumber(Long.parseLong(input.getPhoneNumber()))
                .birthday(input.getBirthday())
                .password(hashPassword(input.getPassword()))
                .enabled(false)
                .roles(checkRoles(input.getRole()))
                .verificationCode(generateVerificationCode())
                .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes))
                .score(100)
                .build();
    }

    private Set<UserRoleEnum> checkRoles(UserRoleEnum userRole) {
        return Optional.ofNullable(userRole)
                .map(Set::of)
                .orElse(Set.of(UserRoleEnum.TENANT));
    }

    private void validateUserForRegistering(RegisterUserRequestDTO input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new UserAlreadyExistsException("A user with this email already exists.");
        }

        if (Period.between(input.getBirthday(), LocalDate.now()).getYears() < 18) {
            throw new UnderRequiredAgeException("User must be at least 18 years old.");
        }
    }

    private String hashPassword(String password) {
        return hashingUtil.encode(password);
    }

    private boolean checkHashPassword(String hashedPassword, String password) {
        return hashingUtil.checkHash(password, hashedPassword);
    }

    private String hashToken(String token) {
        return hashingUtil.hashToken(token);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User Not Found with email: " + email)
        );
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private void invalidateOldTokens(User user) {
        passwordResetTokenRepository.invalidateAllValidTokensForUser(user);
    }

    @Transactional
    public void changePasswordFromProfile(Long userId, ChangePasswordRequestDTO input) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User Not Found!"));

        if (!checkHashPassword(user.getPassword(), input.getCurrentPassword())) {
            throw new WrongPasswordException("Current password is incorrect!");
        }

        user.setPassword(hashPassword(input.getNewPassword()));
        userRepository.save(user);
    }
}

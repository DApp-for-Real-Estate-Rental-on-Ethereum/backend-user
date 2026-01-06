package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.config.UserProfileProducer;
import org.example.userservice.dto.requests.UpdateUserRequestDTO;
import org.example.userservice.dto.requests.UserProfileUpdateRequestDTO;
import org.example.userservice.dto.responses.AdminUserResponseDTO;
import org.example.userservice.dto.responses.UserMeResponseDTO;
import org.example.userservice.dto.responses.UserPublicProfileResponseDTO;
import org.example.userservice.enums.UserRoleEnum;
import org.example.userservice.exception.userException.UserNotFoundException;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final UserProfileProducer userProfileProducer;

    public UserMeResponseDTO findMeById(Long id) {
        User user = findById(id);

        String walletAddr = user.getWalletAddress();
        if (walletAddr != null && walletAddr.trim().isEmpty()) {
            walletAddr = null;
        }
        return new UserMeResponseDTO(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfilePicture(),
                user.getBirthday(),
                user.getPhoneNumber(),
                walletAddr,
                user.getRoles() != null ? user.getRoles() : new HashSet<>(),
                user.getScore() != null ? user.getScore() : 100);
    }

    public UserPublicProfileResponseDTO findUserById(Long id) {
        User user = findById(id);

        return UserPublicProfileResponseDTO
                .builder()
                .profilePicture(user.getProfilePicture())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    public org.example.userservice.dto.responses.UserStatsDTO getUserStats(Long id) {
        User user = findById(id);
        return org.example.userservice.dto.responses.UserStatsDTO.builder()
                .id(user.getId())
                .rating(user.getRating())
                .score(user.getScore())
                .createdAt(user.getCreatedAt())
                .isVerified(user.isEnabled())
                .build();
    }

    @Transactional
    public void updateMe(UpdateUserRequestDTO input, Long id) {
        User user = findById(id);
        boolean walletAddressChanged = false;
        String oldWalletAddress = user.getWalletAddress();

        nonEmpty(input.getFirstName()).ifPresent(user::setFirstName);
        nonEmpty(input.getLastName()).ifPresent(user::setLastName);

        if (input.getWalletAddress() != null) {
            String newWalletAddress = input.getWalletAddress().trim();

            if (!newWalletAddress.isEmpty() && !newWalletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
                throw new IllegalArgumentException(
                        "Wallet address must be a valid Ethereum address (0x followed by 40 hex characters)");
            }

            String processedWalletAddress = newWalletAddress.isEmpty() ? null : newWalletAddress;

            String oldProcessed = (oldWalletAddress != null && !oldWalletAddress.trim().isEmpty())
                    ? oldWalletAddress.trim()
                    : null;
            if (!java.util.Objects.equals(processedWalletAddress, oldProcessed)) {
                user.setWalletAddress(processedWalletAddress);
                walletAddressChanged = true;
            }
        }

        Optional.ofNullable(input.getBirthday())
                .ifPresent(b -> {
                    userValidationService.validateIsAdult(b);
                    user.setBirthday(b);
                });

        userRepository.save(user);

        if (walletAddressChanged) {
            try {
                UserProfileUpdateRequestDTO updateRequest = new UserProfileUpdateRequestDTO();
                updateRequest.setUserId(id.toString());
                updateRequest.setComplete(user.getWalletAddress() != null && !user.getWalletAddress().isEmpty());
                userProfileProducer.sendUserProfileUpdate(updateRequest);
            } catch (Exception e) {
            }
        }
    }

    public List<UserPublicProfileResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> UserPublicProfileResponseDTO.builder()
                        .profilePicture(user.getProfilePicture())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phoneNumber(user.getPhoneNumber())
                        .build())
                .collect(Collectors.toList());
    }

    public List<AdminUserResponseDTO> findAllForAdmin() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    String walletAddr = user.getWalletAddress();
                    if (walletAddr != null && walletAddr.trim().isEmpty()) {
                        walletAddr = null;
                    }

                    return AdminUserResponseDTO.builder()
                            .id(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .profilePicture(user.getProfilePicture())
                            .birthday(user.getBirthday())
                            .phoneNumber(user.getPhoneNumber())
                            .walletAddress(walletAddr)
                            .roles(user.getRoles() != null ? user.getRoles() : new HashSet<>())
                            .enabled(user.isEnabled())
                            .score(user.getScore() != null ? user.getScore() : 100)
                            .rating(user.getRating())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public void addHostRole(Long id) {
        User user = findById(id);

        Set<UserRoleEnum> newRoles = new HashSet<>();
        newRoles.add(UserRoleEnum.HOST);
        user.setRoles(newRoles);
        userRepository.save(user);

        try {
            UserProfileUpdateRequestDTO updateRequest = new UserProfileUpdateRequestDTO();
            updateRequest.setUserId(id.toString());
            updateRequest.setComplete(false);
            userProfileProducer.sendUserProfileUpdate(updateRequest);
        } catch (Exception e) {
        }
    }

    @Transactional
    public void removeHostRole(Long id) {
        User user = findById(id);
        Set<UserRoleEnum> roles = user.getRoles() != null ? new HashSet<>(user.getRoles()) : new HashSet<>();
        roles.remove(UserRoleEnum.HOST);
        if (roles.isEmpty()) {
            roles.add(UserRoleEnum.TENANT);
        }
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Transactional
    public void addAdminRole(Long id) {
        User user = findById(id);
        Set<UserRoleEnum> roles = user.getRoles() != null ? new HashSet<>(user.getRoles()) : new HashSet<>();
        roles.add(UserRoleEnum.ADMIN);
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Transactional
    public void removeAdminRole(Long id) {
        User user = findById(id);
        Set<UserRoleEnum> roles = user.getRoles() != null ? new HashSet<>(user.getRoles()) : new HashSet<>();
        roles.remove(UserRoleEnum.ADMIN);
        if (roles.isEmpty()) {
            roles.add(UserRoleEnum.TENANT);
        }
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long id) {
        User user = findById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void disableUser(Long id) {
        User user = findById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    private Optional<String> nonEmpty(String value) {
        return Optional.ofNullable(value).filter(s -> !s.isBlank());
    }
}

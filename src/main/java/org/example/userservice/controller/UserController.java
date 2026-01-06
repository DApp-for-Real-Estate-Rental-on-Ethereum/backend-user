package org.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.requests.ChangePasswordRequestDTO;
import org.example.userservice.dto.requests.UpdateUserRequestDTO;
import org.example.userservice.dto.responses.AdminUserResponseDTO;
import org.example.userservice.dto.responses.UserMeResponseDTO;
import org.example.userservice.dto.responses.UserPublicProfileResponseDTO;
import org.example.userservice.service.AuthenticationService;
import org.example.userservice.service.UserImageService;
import org.example.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserImageService userImageService;
    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDTO> getMe(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userService.findMeById(Long.parseLong(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateMe(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid UpdateUserRequestDTO updateUserRequestDTO) {
        userService.updateMe(updateUserRequestDTO, Long.parseLong(userId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserPublicProfileResponseDTO> getHost(
            @PathVariable Long id) {
        UserPublicProfileResponseDTO user = userService.findUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<org.example.userservice.dto.responses.UserStatsDTO> getUserStats(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String requesterId,
            @RequestHeader(value = "X-User-Roles", required = false) String requesterRoles) {
        boolean isAdmin = requesterRoles != null && requesterRoles.contains("ADMIN");
        boolean isSelf = requesterId != null && requesterId.equals(id.toString());

        if (!isAdmin && !isSelf) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.getUserStats(id));
    }

    @PutMapping("/me/profile-picture")
    public ResponseEntity<String> updateUserProfilePicture(
            @RequestPart MultipartFile file,
            @RequestHeader("X-User-Id") String userId) {
        String profilePictureUrl = userImageService.uploadProfilePicture(file, Long.parseLong(userId));
        return ResponseEntity.ok().body(profilePictureUrl);
    }

    @DeleteMapping("/me/profile-picture")
    public ResponseEntity<HttpStatus> deleteUserProfilePicture(
            @RequestHeader("X-User-Id") String userId) {
        userImageService.deleteProfilePicture(Long.parseLong(userId));
        return ResponseEntity.ok().body(HttpStatus.OK);
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid ChangePasswordRequestDTO changePasswordRequestDTO) {
        authenticationService.changePasswordFromProfile(Long.parseLong(userId), changePasswordRequestDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/become-host")
    public ResponseEntity<Void> becomeHost(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String requesterId,
            @RequestHeader(value = "X-User-Roles", required = false) String requesterRoles) {
        boolean isAdmin = requesterRoles != null && requesterRoles.contains("ADMIN");
        boolean isSelf = requesterId != null && requesterId.equals(id.toString());

        if (!isAdmin && !isSelf) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.addHostRole(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<AdminUserResponseDTO>> getAllUsersForAdmin(
            @RequestHeader("X-User-Roles") String userRoles) {
        if (userRoles == null || !userRoles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<AdminUserResponseDTO> users = userService.findAllForAdmin();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/admin/{id}/enable")
    public ResponseEntity<Void> enableUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Roles") String userRoles) {
        if (userRoles == null || !userRoles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.enableUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/{id}/disable")
    public ResponseEntity<Void> disableUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Roles") String userRoles) {
        if (userRoles == null || !userRoles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.disableUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/{id}/add-admin-role")
    public ResponseEntity<Void> addAdminRole(
            @PathVariable Long id,
            @RequestHeader("X-User-Roles") String userRoles) {
        if (userRoles == null || !userRoles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.addAdminRole(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/{id}/remove-admin-role")
    public ResponseEntity<Void> removeAdminRole(
            @PathVariable Long id,
            @RequestHeader("X-User-Roles") String userRoles) {
        if (userRoles == null || !userRoles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.removeAdminRole(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/{id}/add-host-role")
    public ResponseEntity<Void> addHostRoleByAdmin(
            @PathVariable Long id,
            @RequestHeader("X-User-Roles") String userRoles) {
        if (userRoles == null || !userRoles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.addHostRole(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/{id}/remove-host-role")
    public ResponseEntity<Void> removeHostRole(
            @PathVariable Long id,
            @RequestHeader("X-User-Roles") String userRoles) {
        if (userRoles == null || !userRoles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.removeHostRole(id);
        return ResponseEntity.ok().build();
    }
}
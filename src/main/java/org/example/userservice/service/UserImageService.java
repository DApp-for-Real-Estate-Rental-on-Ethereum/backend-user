package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.exception.userException.UserNotFoundException;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.serviceInterfaces.IStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserImageService {
    private final IStorageService  storageService;
    private final UserRepository userRepository;

    public String uploadProfilePicture(MultipartFile file, Long userId) {
        User user  = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        String extension = StringUtils.getFilenameExtension(
                Objects.requireNonNullElse(file.getOriginalFilename(), "")
        );
        if (extension == null) {
            throw new IllegalArgumentException("File must have an extension");
        }
        extension = extension.toLowerCase();
        if (!extension.equals("jpeg") && !extension.equals("jpg") && !extension.equals("png") && !extension.equals("img")) {
            throw new IllegalArgumentException("Only JPEG (.jpg, .jpeg), PNG (.png) and IMG (.img) files are allowed");
        }
        String contentType = file.getContentType();
        if (extension.equals("img")) {
        } else {
            if (contentType == null ||
                    !(contentType.equalsIgnoreCase("image/jpeg") || 
                      contentType.equalsIgnoreCase("image/jpg") || 
                      contentType.equalsIgnoreCase("image/png"))) {
                throw new IllegalArgumentException("File MIME type must be image/jpeg, image/jpg, or image/png");
            }
        }
        String fileName = "profile_" + userId.toString() + "." + extension;
        String url = storageService.storeFile(file, fileName);
        Optional.ofNullable(user.getProfilePicture()).ifPresent(storageService::deleteFile);
        user.setProfilePicture(url);
        userRepository.save(user);
        return url;
    }

    public void deleteProfilePicture(Long userId) {
        User user  = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        storageService.deleteFile(user.getProfilePicture());
        user.setProfilePicture(null);
        userRepository.save(user);
    }
}

package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.service.serviceInterfaces.IStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements IStorageService {

    @Value("${app.storage.local.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.storage.local.base-url:http://localhost:8082}")
    private String baseUrl;

    @Value("${app.storage.local.profile-folder:profile-pictures}")
    private String profileFolder;

    @Override
    public String storeFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        return storeFile(file, fileName);
    }

    @Override
    public String storeFile(MultipartFile file, String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir, profileFolder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "/" + profileFolder + "/" + fileName;
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String url) {
        try {
            String filePath = extractFilePathFromUrl(url);
            Path path = Paths.get(uploadDir, filePath);
            
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
        }
    }

    private String extractFilePathFromUrl(String url) {
        if (url.startsWith(baseUrl)) {
            return url.substring(baseUrl.length());
        }
        if (url.startsWith("/")) {
            return url.substring(1);
        }
        return url;
    }
}


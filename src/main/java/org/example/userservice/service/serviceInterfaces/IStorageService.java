package org.example.userservice.service.serviceInterfaces;

import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    String storeFile(MultipartFile file);
    String storeFile(MultipartFile file, String fileName);
    void deleteFile(String url);
}


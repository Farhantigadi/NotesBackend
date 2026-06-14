package com.interviewprep.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    private final Path uploadDir;
    private final String baseUrl;

    public ImageStorageService(@Value("${app.upload.dir:uploads}") String uploadDir,
                               @Value("${app.base-url:http://localhost:8080}") String baseUrl) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        Files.createDirectories(this.uploadDir);
    }

    public String store(MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image: " + e.getMessage());
        }
        return baseUrl + "/uploads/" + filename;
    }

    public void delete(String imageUrl) {
        if (imageUrl == null) return;
        String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        try {
            Files.deleteIfExists(uploadDir.resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}

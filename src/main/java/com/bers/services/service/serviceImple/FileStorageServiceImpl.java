package com.bers.services.service.serviceImple;

import com.bers.services.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload.base-dir:uploads}")
    private String baseUploadDir;

    @Override
    public String storeFile(MultipartFile file, String directory) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Failed to store empty file");
            }

            // Get original filename
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            // Check for invalid characters
            if (originalFilename.contains("..")) {
                throw new IllegalArgumentException("Filename contains invalid path sequence: " + originalFilename);
            }

            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String extension = getFileExtension(originalFilename);
            String newFilename = String.format("%s_%s%s", timestamp, uniqueId, extension);

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(baseUploadDir, directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Copy file to the target location
            Path targetLocation = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for storage in database
            return String.format("/%s/%s/%s", baseUploadDir, directory, newFilename);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    @Override
    public boolean deleteFile(String filename, String directory) {
        try {
            Path filePath = Paths.get(baseUploadDir, directory, filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public String getFilePath(String filename, String directory) {
        Path filePath = Paths.get(baseUploadDir, directory, filename);
        return filePath.toString();
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}

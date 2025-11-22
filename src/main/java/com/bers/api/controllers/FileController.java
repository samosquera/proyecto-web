package com.bers.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    @Value("${file.upload.base-dir:uploads}")
    private String baseUploadDir;

    @GetMapping("/{directory}/{filename:.+}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String directory,
            @PathVariable String filename
    ) {
        try {
            Path filePath = Paths.get(baseUploadDir, directory, filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = "application/octet-stream";
            String filenameLower = filename.toLowerCase();
            if (filenameLower.endsWith(".jpg") || filenameLower.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filenameLower.endsWith(".png")) {
                contentType = "image/png";
            } else if (filenameLower.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filenameLower.endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException ex) {
            log.error("Error reading file: {}/{}", directory, filename, ex);
            return ResponseEntity.badRequest().build();
        }
    }
}

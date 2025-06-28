package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root;

    @Autowired
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        try {
            // Correctly resolve the absolute path within the container
            this.root = Paths.get(uploadDir).toAbsolutePath(); 
            if (Files.notExists(this.root)) {
                // Use createDirectories to create parent directories if they don't exist
                Files.createDirectories(this.root); 
            }
        } catch (IOException e) {
            // Add logging to see the error during startup
            e.printStackTrace(); 
            throw new RuntimeException("Could not initialize folder for upload: " + e.getMessage());
        }
    }

    public String save(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                throw new RuntimeException("Cannot store file with relative path outside current directory " + filename);
            }
            
            // Generate a unique filename
            String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;

            // Copy file to the target location (this will overwrite existing file with the same name)
            Files.copy(file.getInputStream(), this.root.resolve(uniqueFilename), StandardCopyOption.REPLACE_EXISTING);

            // Return the relative path to be stored in the database
            return "/uploads/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }
}
package com.example.demo.service;

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
public class AdminFileStorageService {

    @Value("${frontend.dir:/app/Frontend}")
    private String frontendDir;

    private final Path destinationImagesPath;

    public AdminFileStorageService(@Value("${frontend.dir:/app/Frontend}") String frontendDir) {
        this.frontendDir = frontendDir;
        this.destinationImagesPath = Paths.get(frontendDir, "public", "images");
    }

    public String saveDestinationImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // Create directories if they don't exist
        Files.createDirectories(destinationImagesPath);

        // Generate a file name with original extension
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") ?
            originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String filename = UUID.randomUUID().toString() + extension;

        // Copy the file to the target location
        Path targetLocation = destinationImagesPath.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Return the relative path that will be stored in the database
        return "/images/" + filename;
    }
    
    /**
     * Deletes an image file from the frontend's public/images directory
     * @param imageUrl The image URL to delete (relative to the frontend)
     * @return true if the file was deleted, false otherwise
     */
    public boolean deleteDestinationImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        
        // Extract the filename from the URL
        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        
        try {
            Path filePath = destinationImagesPath.resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log the exception
            e.printStackTrace();
            return false;
        }
    }
}

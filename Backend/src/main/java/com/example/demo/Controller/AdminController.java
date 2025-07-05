package com.example.demo.Controller;

import com.example.demo.dto.CreateFeaturedDestinationRequest;
import com.example.demo.entity.FeaturedDestination;
import com.example.demo.entity.User;
import com.example.demo.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Create a new featured destination
     * 
     * @param user The authenticated user
     * @param request The featured destination details
     * @param image The image file for the featured destination
     * @return The created featured destination
     */
    @PostMapping("/destinations")
    public ResponseEntity<?> createFeaturedDestination(
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("destination") CreateFeaturedDestinationRequest request,
            @RequestPart("image") MultipartFile image) {
        
        // Validate that the user is an admin (for extra security)
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only admin users can perform this action"));
        }
        
        try {
            FeaturedDestination createdDestination = adminService.createFeaturedDestination(request, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDestination);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload image: " + e.getMessage()));
        }
    }

    /**
     * Get all featured destinations
     * 
     * @param user The authenticated user
     * @return List of all featured destinations
     */
    @GetMapping("/destinations")
    public ResponseEntity<?> getAllFeaturedDestinations(
            @AuthenticationPrincipal User user) {
        
        // Validate that the user is an admin
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only admin users can perform this action"));
        }
        
        List<FeaturedDestination> destinations = adminService.getAllFeaturedDestinations();
        return ResponseEntity.ok(destinations);
    }

    /**
     * Delete a featured destination
     * 
     * @param user The authenticated user
     * @param id The ID of the featured destination to delete
     * @return Success message or error
     */
    @DeleteMapping("/destinations/{id}")
    public ResponseEntity<?> deleteFeaturedDestination(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        
        // Validate that the user is an admin
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only admin users can perform this action"));
        }
        
        try {
            adminService.deleteFeaturedDestination(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Featured destination deleted successfully");
            response.put("id", id.toString());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Toggle the active status of a featured destination
     * 
     * @param user The authenticated user
     * @param id The ID of the featured destination to toggle
     * @return The updated featured destination
     */
    @PutMapping("/destinations/{id}/toggle-status")
    public ResponseEntity<?> toggleFeaturedDestinationStatus(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        
        // Validate that the user is an admin
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only admin users can perform this action"));
        }
        
        try {
            FeaturedDestination updatedDestination = adminService.toggleFeaturedDestinationStatus(id);
            return ResponseEntity.ok(updatedDestination);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

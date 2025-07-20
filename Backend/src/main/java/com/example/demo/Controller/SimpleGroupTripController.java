package com.example.demo.Controller;

import com.example.demo.Repository.GroupTripRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.entity.GroupTrip;
import com.example.demo.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/group-trips-simple")
@RequiredArgsConstructor
public class SimpleGroupTripController {

    private final GroupTripRepository groupTripRepository;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createGroupTrip(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            System.out.println("=== SIMPLE DEBUG: Group Trip Creation Started ===");
            System.out.println("Request: " + request);
            System.out.println("Authentication: " + authentication.getName());
            
            // Get user
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            System.out.println("SIMPLE: User found: " + user.getUsername());
            
            // Create group trip directly
            GroupTrip groupTrip = GroupTrip.builder()
                    .groupName((String) request.get("groupName"))
                    .description((String) request.get("description"))
                    .maxPeople(5)
                    .tripPlanId(1L) // Fixed for testing
                    .createdByUserId(user.getId())
                    .currentMembers(1)
                    .status(GroupTrip.GroupTripStatus.OPEN)
                    .build();
            
            System.out.println("SIMPLE: About to save: " + groupTrip.getGroupName());
            
            // Save directly
            GroupTrip saved = groupTripRepository.save(groupTrip);
            
            System.out.println("SIMPLE: Saved with ID: " + saved.getId());
            
            // Check count
            long count = groupTripRepository.count();
            System.out.println("SIMPLE: Total in database: " + count);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Group trip created successfully (test)",
                "id", saved.getId(),
                "count", count
            ));
            
        } catch (Exception e) {
            System.out.println("SIMPLE: ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}

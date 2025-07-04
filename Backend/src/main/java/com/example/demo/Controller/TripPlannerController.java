package com.example.demo.Controller;

import com.example.demo.service.TripPlannerService;
import com.example.demo.service.AcceptedTripService;
import com.example.demo.dto.TripPlanRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trip")
public class TripPlannerController {

    @Autowired
    private TripPlannerService tripPlannerService;

    @Autowired
    private AcceptedTripService acceptedTripService;    @PostMapping("/plan")
    public ResponseEntity<?> planTrip(@RequestBody TripPlanRequest request, Authentication authentication) {
        try {
            // Get authenticated user details
            String userEmail = authentication.getName();
            
            // Debug logging
            System.out.println("🔍 SPRING BOOT RECEIVED REQUEST:");
            System.out.println("User: " + userEmail);
            System.out.println("Destination: " + request.getDestination());
            System.out.println("Start Date: " + request.getStartDate());
            System.out.println("Duration Days: " + request.getDurationDays());
            System.out.println("Budget: " + request.getBudget());
            System.out.println("Origin: " + request.getOrigin());
            
            // Call the service to handle trip planning
            Map<String, Object> tripPlan = tripPlannerService.planTrip(request, userEmail);
            
            return ResponseEntity.ok(tripPlan);
        } catch (Exception e) {
            System.err.println("❌ Error in TripPlannerController: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "trip-planner-gateway"
        ));
    }

    /**
     * Accept a trip plan and save it to database
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptTripPlan(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            // Extract trip plan from request
            Map<String, Object> tripPlan = (Map<String, Object>) request.get("tripPlan");
            if (tripPlan == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Trip plan is required"
                ));
            }

            // Accept and save the trip
            var acceptedTrip = acceptedTripService.acceptTrip(userEmail, tripPlan);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Trip accepted and saved successfully",
                "tripId", acceptedTrip.getId(),
                "createdAt", acceptedTrip.getCreatedAt()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error accepting trip: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Alternative endpoint for accepting trips (alias for /accept)
     * Used by travel service or other components
     */
    @PostMapping("/save-trip")
    public ResponseEntity<?> saveTripPlan(@RequestBody Map<String, Object> request, Authentication authentication) {
        return acceptTripPlan(request, authentication);
    }

    /**
     * Customize an existing trip plan based on user's modification request
     */
    @PostMapping("/customize")
    public ResponseEntity<?> customizeTrip(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            // Extract original trip plan and user prompt from request
            Map<String, Object> originalPlan = (Map<String, Object>) request.get("originalPlan");
            String userPrompt = (String) request.get("userPrompt");
            Long tripId = request.get("tripId") != null ? Long.valueOf(request.get("tripId").toString()) : null;
            
            if (originalPlan == null || userPrompt == null || userPrompt.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Original trip plan and modification request are required"
                ));
            }

            System.out.println("🔄 CUSTOMIZING TRIP:");
            System.out.println("User: " + userEmail);
            System.out.println("Trip ID: " + tripId);
            System.out.println("User Prompt: " + userPrompt);
            
            // Call the service to handle trip customization
            Map<String, Object> customizedPlan = tripPlannerService.customizeTrip(originalPlan, userPrompt, userEmail);
            
            return ResponseEntity.ok(customizedPlan);
        } catch (Exception e) {
            System.err.println("❌ Error customizing trip: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}

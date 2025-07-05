package com.example.demo.Controller;

import com.example.demo.entity.TripPlan;
import com.example.demo.service.TripPlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trip-plans")
public class TripPlanController {

    @Autowired
    private TripPlanService tripPlanService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Accept a trip plan and save it to database
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptTrip(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            
            // Extract trip plan from request
            Map<String, Object> tripPlan = (Map<String, Object>) request.get("tripPlan");
            if (tripPlan == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Trip plan is required"
                ));
            }

            // Extract status from request (optional, defaults to UPCOMING)
            String statusStr = (String) request.get("status");
            TripPlan.TripStatus status = TripPlan.TripStatus.UPCOMING;
            if (statusStr != null) {
                try {
                    status = TripPlan.TripStatus.fromValue(statusStr.toLowerCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Invalid status. Must be one of: upcoming, running, completed"
                    ));
                }
            }

            // Accept and save the trip
            TripPlan savedTripPlan = tripPlanService.acceptTrip(userIdentifier, tripPlan, status);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Trip accepted successfully",
                "tripId", savedTripPlan.getId(),
                "status", savedTripPlan.getStatus().getValue(),
                "createdAt", savedTripPlan.getCreatedAt()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error accepting trip: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to accept trip: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all trip plans for the authenticated user
     */
    @GetMapping("/my-trips")
    public ResponseEntity<?> getMyTripPlans(Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            
            List<TripPlan> tripPlans = tripPlanService.getTripPlansByUser(userIdentifier);
            
            // Convert to response format
            List<Map<String, Object>> response = tripPlans.stream().map(tripPlan -> {
                try {
                    Map<String, Object> tripResponse = new HashMap<>();
                    tripResponse.put("id", tripPlan.getId());
                    tripResponse.put("status", tripPlan.getStatus().getValue());
                    tripResponse.put("createdAt", tripPlan.getCreatedAt());
                    
                    // Parse the JSON trip plan
                    Map<String, Object> tripPlanData = objectMapper.readValue(tripPlan.getTripPlan(), Map.class);
                    tripResponse.put("tripPlan", tripPlanData);
                    
                    return tripResponse;
                } catch (Exception e) {
                    System.err.println("❌ Error parsing trip plan JSON: " + e.getMessage());
                    return null;
                }
            }).filter(tripResponse -> tripResponse != null).toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "trips", response,
                "total", response.size()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error fetching trip plans: " + e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to fetch trip plans: " + e.getMessage()
            ));
        }
    }

    /**
     * Get trip plans by status
     */
    @GetMapping("/my-trips/{status}")
    public ResponseEntity<?> getTripPlansByStatus(@PathVariable String status, Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            
            // Validate status
            TripPlan.TripStatus tripStatus;
            try {
                tripStatus = TripPlan.TripStatus.fromValue(status.toLowerCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid status. Must be one of: upcoming, running, completed"
                ));
            }
            
            List<TripPlan> tripPlans = tripPlanService.getTripPlansByUserAndStatus(userIdentifier, tripStatus);
            
            // Convert to response format
            List<Map<String, Object>> response = tripPlans.stream().map(tripPlan -> {
                try {
                    Map<String, Object> tripResponse = new HashMap<>();
                    tripResponse.put("id", tripPlan.getId());
                    tripResponse.put("status", tripPlan.getStatus().getValue());
                    tripResponse.put("createdAt", tripPlan.getCreatedAt());
                    
                    // Parse the JSON trip plan
                    Map<String, Object> tripPlanData = objectMapper.readValue(tripPlan.getTripPlan(), Map.class);
                    tripResponse.put("tripPlan", tripPlanData);
                    
                    return tripResponse;
                } catch (Exception e) {
                    System.err.println("❌ Error parsing trip plan JSON: " + e.getMessage());
                    return null;
                }
            }).filter(tripResponse -> tripResponse != null).toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "trips", response,
                "status", status.toLowerCase(),
                "total", response.size()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error fetching trip plans by status: " + e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to fetch trip plans: " + e.getMessage()
            ));
        }
    }

    /**
     * Get categorized trips (upcoming, running, completed)
     */
    @GetMapping("/categorized")
    public ResponseEntity<?> getCategorizedTrips(Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            
            Map<String, Object> categorizedTrips = tripPlanService.getCategorizedTrips(userIdentifier);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", categorizedTrips
            ));

        } catch (Exception e) {
            System.err.println("❌ Error fetching categorized trips: " + e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to fetch categorized trips: " + e.getMessage()
            ));
        }
    }

    /**
     * Update trip status
     */
    @PutMapping("/{tripId}/status")
    public ResponseEntity<?> updateTripStatus(@PathVariable Long tripId, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String newStatusStr = request.get("status");
            if (newStatusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Status is required"
                ));
            }

            // Validate status
            TripPlan.TripStatus newStatus;
            try {
                newStatus = TripPlan.TripStatus.fromValue(newStatusStr.toLowerCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid status. Must be one of: upcoming, running, completed"
                ));
            }

            TripPlan updatedTripPlan = tripPlanService.updateTripStatus(tripId, newStatus);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Trip status updated successfully",
                "tripId", updatedTripPlan.getId(),
                "newStatus", updatedTripPlan.getStatus().getValue()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error updating trip status: " + e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to update trip status: " + e.getMessage()
            ));
        }
    }

    /**
     * Delete a trip plan
     */
    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteTripPlan(@PathVariable Long tripId, Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            
            tripPlanService.deleteTripPlan(tripId, userIdentifier);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Trip plan deleted successfully"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error deleting trip plan: " + e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to delete trip plan: " + e.getMessage()
            ));
        }
    }
}

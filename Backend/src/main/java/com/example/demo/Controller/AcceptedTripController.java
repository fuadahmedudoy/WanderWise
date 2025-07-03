package com.example.demo.Controller;

import com.example.demo.entity.AcceptedTrip;
import com.example.demo.service.AcceptedTripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/accepted-trips")
public class AcceptedTripController {

    @Autowired
    private AcceptedTripService acceptedTripService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Accept a trip plan and save it to database
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptTrip(@RequestBody Map<String, Object> request, Authentication authentication) {
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
            AcceptedTrip acceptedTrip = acceptedTripService.acceptTrip(userEmail, tripPlan);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Trip accepted successfully",
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
     * Get all accepted trips for the authenticated user
     */
    @GetMapping("/my-trips")
    public ResponseEntity<?> getMyAcceptedTrips(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<AcceptedTrip> trips = acceptedTripService.getUserAcceptedTrips(userEmail);
            
            // Convert trip plans from JSON strings to objects
            List<Map<String, Object>> tripsWithParsedPlans = trips.stream()
                .map(trip -> {
                    try {
                        Map<String, Object> tripData = new HashMap<>();
                        tripData.put("id", trip.getId());
                        tripData.put("createdAt", trip.getCreatedAt());
                        tripData.put("tripPlan", objectMapper.readValue(trip.getTripPlan(), Map.class));
                        return tripData;
                    } catch (Exception e) {
                        System.err.println("❌ Error parsing trip plan: " + e.getMessage());
                        Map<String, Object> tripData = new HashMap<>();
                        tripData.put("id", trip.getId());
                        tripData.put("createdAt", trip.getCreatedAt());
                        tripData.put("tripPlan", null);
                        tripData.put("error", "Failed to parse trip plan");
                        return tripData;
                    }
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "trips", tripsWithParsedPlans,
                "totalCount", trips.size()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching user trips: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get a specific accepted trip by ID
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getAcceptedTripById(@PathVariable Long tripId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Optional<AcceptedTrip> tripOptional = acceptedTripService.getAcceptedTripById(tripId);
            
            if (!tripOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            AcceptedTrip trip = tripOptional.get();
            
            // Parse the trip plan JSON
            Map<String, Object> tripData = new HashMap<>();
            tripData.put("id", trip.getId());
            tripData.put("createdAt", trip.getCreatedAt());
            try {
                tripData.put("tripPlan", objectMapper.readValue(trip.getTripPlan(), Map.class));
            } catch (Exception e) {
                tripData.put("tripPlan", null);
                tripData.put("error", "Failed to parse trip plan");
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "trip", tripData
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching trip: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get recent accepted trips (last 30 days)
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentAcceptedTrips(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<AcceptedTrip> trips = acceptedTripService.getRecentAcceptedTrips(userEmail);
            
            // Convert trip plans from JSON strings to objects
            List<Map<String, Object>> tripsWithParsedPlans = trips.stream()
                .map(trip -> {
                    try {
                        Map<String, Object> tripData = new HashMap<>();
                        tripData.put("id", trip.getId());
                        tripData.put("createdAt", trip.getCreatedAt());
                        tripData.put("tripPlan", objectMapper.readValue(trip.getTripPlan(), Map.class));
                        return tripData;
                    } catch (Exception e) {
                        System.err.println("❌ Error parsing trip plan: " + e.getMessage());
                        Map<String, Object> tripData = new HashMap<>();
                        tripData.put("id", trip.getId());
                        tripData.put("createdAt", trip.getCreatedAt());
                        tripData.put("tripPlan", null);
                        tripData.put("error", "Failed to parse trip plan");
                        return tripData;
                    }
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "trips", tripsWithParsedPlans,
                "totalCount", trips.size()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching recent trips: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get trip statistics for the authenticated user
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getTripStats(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            long totalTrips = acceptedTripService.countUserAcceptedTrips(userEmail);
            List<AcceptedTrip> recentTrips = acceptedTripService.getRecentAcceptedTrips(userEmail);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", Map.of(
                    "totalAcceptedTrips", totalTrips,
                    "recentTrips", recentTrips.size(),
                    "lastTripDate", recentTrips.isEmpty() ? null : recentTrips.get(0).getCreatedAt()
                )
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching trip stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Delete an accepted trip
     */
    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteAcceptedTrip(@PathVariable Long tripId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            boolean deleted = acceptedTripService.deleteAcceptedTrip(tripId, userEmail);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Trip deleted successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting trip: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}

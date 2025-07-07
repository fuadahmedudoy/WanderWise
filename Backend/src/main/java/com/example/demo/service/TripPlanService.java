package com.example.demo.service;

import com.example.demo.entity.TripPlan;
import com.example.demo.entity.User;
import com.example.demo.Repository.TripPlanRepository;
import com.example.demo.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TripPlanService {

    @Autowired
    private TripPlanRepository tripPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Accept a trip plan and save it to the database
     */
    public TripPlan acceptTrip(String userIdentifier, Map<String, Object> tripPlan) {
        return acceptTrip(userIdentifier, tripPlan, TripPlan.TripStatus.UPCOMING);
    }

    /**
     * Accept a trip plan and save it to the database with specific status
     */
    public TripPlan acceptTrip(String userIdentifier, Map<String, Object> tripPlan, TripPlan.TripStatus status) {
        try {
            // Find user by username first, then by email if not found
            Optional<User> userOptional = userRepository.findByUsername(userIdentifier);
            if (!userOptional.isPresent()) {
                userOptional = userRepository.findByEmail(userIdentifier);
            }
            
            if (!userOptional.isPresent()) {
                throw new RuntimeException("User not found with identifier: " + userIdentifier);
            }

            User user = userOptional.get();
            
            // Extract trip summary data for debugging and validation
            Map<String, Object> tripSummary = null;
            String destination = null;
            String origin = null;
            Integer duration = null;
            Integer budget = null;

            if (tripPlan.containsKey("trip_summary")) {
                tripSummary = (Map<String, Object>) tripPlan.get("trip_summary");
                destination = (String) tripSummary.get("destination");
                origin = (String) tripSummary.get("origin");
                
                // Handle duration - could be Integer or Double
                Object durationObj = tripSummary.get("duration");
                if (durationObj instanceof Integer) {
                    duration = (Integer) durationObj;
                } else if (durationObj instanceof Double) {
                    duration = ((Double) durationObj).intValue();
                } else if (durationObj instanceof Number) {
                    duration = ((Number) durationObj).intValue();
                }
                
                // Handle budget - could be Integer or Double
                Object budgetObj = tripSummary.get("total_budget");
                if (budgetObj instanceof Integer) {
                    budget = (Integer) budgetObj;
                } else if (budgetObj instanceof Double) {
                    budget = ((Double) budgetObj).intValue();
                } else if (budgetObj instanceof Number) {
                    budget = ((Number) budgetObj).intValue();
                }
            }

            // Log the data being saved
            System.out.println("💾 SAVING TRIP TO DATABASE:");
            System.out.println("   User: " + user.getUsername() + " (ID: " + user.getId() + ")");
            System.out.println("   Destination: " + destination);
            System.out.println("   Origin: " + origin);
            System.out.println("   Duration: " + duration + " days");
            System.out.println("   Budget: ৳" + budget);
            System.out.println("   Status: " + status);
            System.out.println("   Trip plan keys: " + tripPlan.keySet());

            // Convert trip plan to JSON string
            String tripPlanJson = objectMapper.writeValueAsString(tripPlan);
            
            // Create and save the trip plan entity
            TripPlan tripPlanEntity = TripPlan.builder()
                    .userId(user.getId())
                    .tripPlan(tripPlanJson)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build();

            TripPlan savedTripPlan = tripPlanRepository.save(tripPlanEntity);
            
            System.out.println("✅ Trip saved successfully with ID: " + savedTripPlan.getId());
            
            return savedTripPlan;

        } catch (Exception e) {
            System.err.println("❌ Error saving trip: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save trip: " + e.getMessage(), e);
        }
    }

    /**
     * Get all trip plans for a user
     */
    public List<TripPlan> getTripPlansByUser(String userIdentifier) {
        try {
            // Find user by username first, then by email if not found
            Optional<User> userOptional = userRepository.findByUsername(userIdentifier);
            if (!userOptional.isPresent()) {
                userOptional = userRepository.findByEmail(userIdentifier);
            }
            
            if (!userOptional.isPresent()) {
                throw new RuntimeException("User not found with identifier: " + userIdentifier);
            }

            User user = userOptional.get();
            return tripPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        } catch (Exception e) {
            System.err.println("❌ Error fetching user trips: " + e.getMessage());
            throw new RuntimeException("Failed to fetch trips: " + e.getMessage(), e);
        }
    }

    /**
     * Get trip plans by user and status
     */
    public List<TripPlan> getTripPlansByUserAndStatus(String userIdentifier, TripPlan.TripStatus status) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(userIdentifier);
            if (!userOptional.isPresent()) {
                userOptional = userRepository.findByEmail(userIdentifier);
            }
            
            if (!userOptional.isPresent()) {
                throw new RuntimeException("User not found with identifier: " + userIdentifier);
            }

            User user = userOptional.get();
            return tripPlanRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status);

        } catch (Exception e) {
            System.err.println("❌ Error fetching user trips by status: " + e.getMessage());
            throw new RuntimeException("Failed to fetch trips by status: " + e.getMessage(), e);
        }
    }

    /**
     * Update trip plan status
     */
    public TripPlan updateTripStatus(Long tripId, TripPlan.TripStatus newStatus) {
        try {
            Optional<TripPlan> tripOptional = tripPlanRepository.findById(tripId);
            if (!tripOptional.isPresent()) {
                throw new RuntimeException("Trip plan not found with ID: " + tripId);
            }

            TripPlan tripPlan = tripOptional.get();
            tripPlan.setStatus(newStatus);
            
            return tripPlanRepository.save(tripPlan);

        } catch (Exception e) {
            System.err.println("❌ Error updating trip status: " + e.getMessage());
            throw new RuntimeException("Failed to update trip status: " + e.getMessage(), e);
        }
    }

    /**
     * Get categorized trips (upcoming, running, completed)
     */
    public Map<String, Object> getCategorizedTrips(String userIdentifier) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(userIdentifier);
            if (!userOptional.isPresent()) {
                userOptional = userRepository.findByEmail(userIdentifier);
            }
            
            if (!userOptional.isPresent()) {
                throw new RuntimeException("User not found with identifier: " + userIdentifier);
            }

            User user = userOptional.get();
            
            List<TripPlan> upcomingTrips = tripPlanRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), TripPlan.TripStatus.UPCOMING);
            List<TripPlan> runningTrips = tripPlanRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), TripPlan.TripStatus.RUNNING);
            List<TripPlan> completedTrips = tripPlanRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), TripPlan.TripStatus.COMPLETED);

            // Convert to response format
            Map<String, Object> response = new HashMap<>();
            response.put("upcoming", convertToTripPlanResponses(upcomingTrips));
            response.put("running", convertToTripPlanResponses(runningTrips));
            response.put("completed", convertToTripPlanResponses(completedTrips));
            response.put("total", upcomingTrips.size() + runningTrips.size() + completedTrips.size());

            return response;

        } catch (Exception e) {
            System.err.println("❌ Error getting categorized trips: " + e.getMessage());
            throw new RuntimeException("Failed to get categorized trips: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a trip plan
     */
    public void deleteTripPlan(Long tripId, String userIdentifier) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(userIdentifier);
            if (!userOptional.isPresent()) {
                userOptional = userRepository.findByEmail(userIdentifier);
            }
            
            if (!userOptional.isPresent()) {
                throw new RuntimeException("User not found with identifier: " + userIdentifier);
            }

            User user = userOptional.get();
            
            Optional<TripPlan> tripOptional = tripPlanRepository.findById(tripId);
            if (!tripOptional.isPresent()) {
                throw new RuntimeException("Trip plan not found with ID: " + tripId);
            }

            TripPlan tripPlan = tripOptional.get();
            
            // Verify ownership
            if (!tripPlan.getUserId().equals(user.getId())) {
                throw new RuntimeException("User not authorized to delete this trip plan");
            }

            tripPlanRepository.delete(tripPlan);
            
            System.out.println("✅ Trip plan deleted successfully: " + tripId);

        } catch (Exception e) {
            System.err.println("❌ Error deleting trip plan: " + e.getMessage());
            throw new RuntimeException("Failed to delete trip plan: " + e.getMessage(), e);
        }
    }

    // Helper method to convert TripPlan entities to response format
    private List<Map<String, Object>> convertToTripPlanResponses(List<TripPlan> tripPlans) {
        List<Map<String, Object>> responses = new ArrayList<>();
        
        for (TripPlan tripPlan : tripPlans) {
            try {
                Map<String, Object> response = new HashMap<>();
                response.put("id", tripPlan.getId());
                response.put("status", tripPlan.getStatus().getValue());
                response.put("createdAt", tripPlan.getCreatedAt());
                
                // Parse the JSON trip plan
                Map<String, Object> tripPlanData = objectMapper.readValue(tripPlan.getTripPlan(), Map.class);
                response.put("tripPlan", tripPlanData);
                
                responses.add(response);
                
            } catch (Exception e) {
                System.err.println("❌ Error parsing trip plan JSON: " + e.getMessage());
                // Skip this trip plan if JSON parsing fails
                continue;
            }
        }
        
        return responses;
    }
}

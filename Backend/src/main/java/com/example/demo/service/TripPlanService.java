package com.example.demo.service;

import com.example.demo.Repository.TripActivityRepository;
import com.example.demo.entity.TripActivity;
import com.example.demo.entity.TripPlan;
import com.example.demo.entity.User;
import com.example.demo.Repository.TripPlanRepository;
import com.example.demo.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private TripActivityRepository tripActivityRepository;
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
     * Scheduled task to automatically update trip status based on dates
     * Runs daily at midnight
     * @return A map containing information about the update operation
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public Map<String, Object> autoUpdateTripStatus() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        System.out.println("\n" + "=".repeat(90));
        System.out.println("🔄 AUTOMATED TRIP STATUS UPDATE - " + timestamp);
        System.out.println("=".repeat(90));
        
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", timestamp);
        
        try {
            LocalDate today = LocalDate.now();
            int updatedCount = 0;
            int upcomingToRunning = 0;
            int runningToCompleted = 0;
            
            // Get all upcoming trips
            List<TripPlan> upcomingTrips = tripPlanRepository.findByStatus(TripPlan.TripStatus.UPCOMING);
            System.out.println("📊 Found " + upcomingTrips.size() + " upcoming trips to check");
            result.put("upcomingTripsChecked", upcomingTrips.size());
            
            for (TripPlan trip : upcomingTrips) {
                LocalDate startDate = trip.getStartDate();
                Integer durationDays = trip.getDurationDays();
                
                if (startDate != null && durationDays != null) {
                    LocalDate endDate = startDate.plusDays(durationDays);
                    
                    // If start date is today or in the past, but end date is in the future
                    if ((startDate.isEqual(today) || startDate.isBefore(today)) && endDate.isAfter(today)) {
                        System.out.println("🔄 Updating trip #" + trip.getId() + " to RUNNING status");
                        System.out.println("   📍 Destination: " + trip.getDestination());
                        System.out.println("   📅 Start date: " + startDate + ", End date: " + endDate);
                        
                        trip.setStatus(TripPlan.TripStatus.RUNNING);
                        tripPlanRepository.save(trip);
                        updatedCount++;
                        upcomingToRunning++;
                    }
                }
            }
            
            // Get all running trips
            List<TripPlan> runningTrips = tripPlanRepository.findByStatus(TripPlan.TripStatus.RUNNING);
            System.out.println("📊 Found " + runningTrips.size() + " running trips to check");
            result.put("runningTripsChecked", runningTrips.size());
            
            for (TripPlan trip : runningTrips) {
                LocalDate startDate = trip.getStartDate();
                Integer durationDays = trip.getDurationDays();
                
                if (startDate != null && durationDays != null) {
                    LocalDate endDate = startDate.plusDays(durationDays);
                    
                    // If end date has passed
                    if (endDate.isBefore(today)) {
                        System.out.println("🔄 Updating trip #" + trip.getId() + " to COMPLETED status");
                        System.out.println("   📍 Destination: " + trip.getDestination());
                        System.out.println("   📅 Start date: " + startDate + ", End date: " + endDate);
                        
                        trip.setStatus(TripPlan.TripStatus.COMPLETED);
                        tripPlanRepository.save(trip);
                        updatedCount++;
                        runningToCompleted++;
                    }
                }
            }
            
            System.out.println("✅ Auto-update completed. Updated " + updatedCount + " trips.");
            System.out.println("   - " + upcomingToRunning + " trips changed from UPCOMING to RUNNING");
            System.out.println("   - " + runningToCompleted + " trips changed from RUNNING to COMPLETED");
            System.out.println("=".repeat(90) + "\n");
            
            result.put("updatedCount", updatedCount);
            result.put("upcomingToRunning", upcomingToRunning);
            result.put("runningToCompleted", runningToCompleted);
            result.put("success", true);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error during auto trip status update: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Check if there are any trips that need status updates
     * @return Map with information about trips that need updating
     */
    public Map<String, Object> checkTripsNeedingStatusUpdate() {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        try {
            // Check upcoming trips that should be running
            List<TripPlan> upcomingTrips = tripPlanRepository.findByStatus(TripPlan.TripStatus.UPCOMING);
            int upcomingNeedingUpdate = 0;
            
            for (TripPlan trip : upcomingTrips) {
                LocalDate startDate = trip.getStartDate();
                Integer durationDays = trip.getDurationDays();
                
                if (startDate != null && durationDays != null) {
                    LocalDate endDate = startDate.plusDays(durationDays);
                    
                    if ((startDate.isEqual(today) || startDate.isBefore(today)) && endDate.isAfter(today)) {
                        upcomingNeedingUpdate++;
                    }
                }
            }
            
            // Check running trips that should be completed
            List<TripPlan> runningTrips = tripPlanRepository.findByStatus(TripPlan.TripStatus.RUNNING);
            int runningNeedingUpdate = 0;
            
            for (TripPlan trip : runningTrips) {
                LocalDate startDate = trip.getStartDate();
                Integer durationDays = trip.getDurationDays();
                
                if (startDate != null && durationDays != null) {
                    LocalDate endDate = startDate.plusDays(durationDays);
                    
                    if (endDate.isBefore(today)) {
                        runningNeedingUpdate++;
                    }
                }
            }
            
            result.put("upcomingNeedingUpdate", upcomingNeedingUpdate);
            result.put("runningNeedingUpdate", runningNeedingUpdate);
            result.put("totalNeedingUpdate", upcomingNeedingUpdate + runningNeedingUpdate);
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Delete a trip plan
     */
    public boolean deleteTripPlan(Long tripId, String userIdentifier) {
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
            return true;

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


    // ---------- Trip Activity CheckList -------------------
    public Map<String, Boolean> getActivityByTripId(long id){
        Optional<TripPlan> tripPlan=tripPlanRepository.findById(id);
        String jsonString= tripPlan.get().getTripPlan();
        System.out.println(jsonString);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<String> activitySpots = new ArrayList<>();
        Map<String,Boolean> activities=new HashMap<>();
        JsonNode itinerary = root.path("daily_itinerary");
        for (JsonNode day : itinerary) {
            // Morning activity
            JsonNode morning = day.path("morning_activity");
            if (!morning.isMissingNode()) {
                String s=morning.path("spot_name").asText();
                TripActivity tripActivity=new TripActivity();
                tripActivity.setTripId(id);
                tripActivity.setActivity(s);
                tripActivity.setCompleted(false);
                tripActivityRepository.save(tripActivity);
                activitySpots.add(s);
                activities.put(s,false);
            }

            // Afternoon activities
            JsonNode afternoon = day.path("afternoon_activities");
            if (afternoon.isArray()) {
                for (JsonNode activity : afternoon) {
                    String s=activity.path("spot_name").asText();
                    TripActivity tripActivity=new TripActivity();
                    tripActivity.setTripId(id);
                    tripActivity.setActivity(s);
                    tripActivity.setCompleted(false);
                    tripActivityRepository.save(tripActivity);

                    activitySpots.add(s);
                    activities.put(s,false);
                }
            }
        }
        return activities;
    }
    public void updateCheckList(Long tripId,String activity,boolean completed){
        TripActivity tripActivity=tripActivityRepository.findByTripIdAndActivity(tripId,activity);
        tripActivity.setCompleted(completed);
        tripActivityRepository.save(tripActivity);
    }
}

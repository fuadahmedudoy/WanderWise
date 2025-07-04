package com.example.demo.service;

import com.example.demo.entity.AcceptedTrip;
import com.example.demo.entity.User;
import com.example.demo.Repository.AcceptedTripRepository;
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
public class AcceptedTripService {

    @Autowired
    private AcceptedTripRepository acceptedTripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Accept a trip plan and save it to the database
     */
    public AcceptedTrip acceptTrip(String userIdentifier, Map<String, Object> tripPlan) {
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
            Integer durationDays = null;
            Double budget = null;
            String startDate = null;
            String origin = null;
            
            if (tripPlan.containsKey("trip_summary")) {
                tripSummary = (Map<String, Object>) tripPlan.get("trip_summary");
                if (tripSummary != null) {
                    destination = (String) tripSummary.get("destination");
                    durationDays = (Integer) tripSummary.get("duration");
                    Object budgetObj = tripSummary.get("total_budget");
                    if (budgetObj instanceof Number) {
                        budget = ((Number) budgetObj).doubleValue();
                    }
                    startDate = (String) tripSummary.get("start_date");
                    origin = (String) tripSummary.get("origin");
                }
            }
            
            // Debug: Print the trip plan data structure
            System.out.println("🔍 DEBUG: Trip plan data received:");
            System.out.println("Keys: " + tripPlan.keySet());
            System.out.println("Destination: " + destination);
            System.out.println("Duration Days: " + durationDays);
            System.out.println("Budget: " + budget);
            System.out.println("Start Date: " + startDate);
            System.out.println("Origin: " + origin);
            
            // Convert trip plan to JSON string
            String tripPlanJson = objectMapper.writeValueAsString(tripPlan);
            
            // Create and save accepted trip
            AcceptedTrip acceptedTrip = AcceptedTrip.builder()
                    .userId(user.getId())
                    .tripPlan(tripPlanJson)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            AcceptedTrip savedTrip = acceptedTripRepository.save(acceptedTrip);
            
            System.out.println("✅ Trip accepted and saved for user: " + userIdentifier);
            System.out.println("📊 Trip ID: " + savedTrip.getId());
            
            return savedTrip;
            
        } catch (Exception e) {
            System.err.println("❌ Error accepting trip: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to accept trip: " + e.getMessage());
        }
    }

    /**
     * Get all accepted trips for a user
     */
    public List<AcceptedTrip> getUserAcceptedTrips(String userIdentifier) {
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
            return acceptedTripRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching user trips: " + e.getMessage());
            throw new RuntimeException("Failed to fetch user trips: " + e.getMessage());
        }
    }

    /**
     * Get accepted trip by ID
     */
    public Optional<AcceptedTrip> getAcceptedTripById(Long tripId) {
        return acceptedTripRepository.findById(tripId);
    }

    /**
     * Get recent accepted trips for a user (last 30 days)
     */
    public List<AcceptedTrip> getRecentAcceptedTrips(String userEmail) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            if (!userOptional.isPresent()) {
                throw new RuntimeException("User not found with email: " + userEmail);
            }

            User user = userOptional.get();
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            return acceptedTripRepository.findRecentTripsByUserId(user.getId(), thirtyDaysAgo);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching recent trips: " + e.getMessage());
            throw new RuntimeException("Failed to fetch recent trips: " + e.getMessage());
        }
    }

    /**
     * Count total accepted trips for a user
     */
    public long countUserAcceptedTrips(String userEmail) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            if (!userOptional.isPresent()) {
                return 0;
            }

            User user = userOptional.get();
            return acceptedTripRepository.countByUserId(user.getId());
            
        } catch (Exception e) {
            System.err.println("❌ Error counting user trips: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Delete an accepted trip
     */
    public boolean deleteAcceptedTrip(Long tripId, String userIdentifier) {
        try {
            System.out.println("🔍 DEBUG: Attempting to delete trip " + tripId + " for user: " + userIdentifier);
            
            Optional<AcceptedTrip> tripOptional = acceptedTripRepository.findById(tripId);
            if (!tripOptional.isPresent()) {
                System.out.println("❌ Trip not found with ID: " + tripId);
                return false;
            }

            AcceptedTrip trip = tripOptional.get();
            System.out.println("🔍 DEBUG: Found trip with user ID: " + trip.getUserId());
            
            // Find user by username first, then by email if not found (consistent with other methods)
            Optional<User> userOptional = userRepository.findByUsername(userIdentifier);
            if (!userOptional.isPresent()) {
                userOptional = userRepository.findByEmail(userIdentifier);
            }
            
            if (!userOptional.isPresent()) {
                System.out.println("❌ User not found with identifier: " + userIdentifier);
                throw new RuntimeException("User not found with identifier: " + userIdentifier);
            }
            
            User user = userOptional.get();
            System.out.println("🔍 DEBUG: Found user with ID: " + user.getId());
            System.out.println("🔍 DEBUG: User email: " + user.getEmail());
            System.out.println("🔍 DEBUG: User username: " + user.getUsername());
            
            // Verify the trip belongs to the user
            if (!trip.getUserId().equals(user.getId())) {
                System.out.println("❌ Authorization failed: Trip user ID (" + trip.getUserId() + ") != Current user ID (" + user.getId() + ")");
                throw new RuntimeException("Unauthorized to delete this trip");
            }

            System.out.println("✅ Authorization successful, deleting trip...");
            acceptedTripRepository.delete(trip);
            System.out.println("✅ Trip deleted successfully");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting trip: " + e.getMessage());
            throw new RuntimeException("Failed to delete trip: " + e.getMessage());
        }
    }

    /**
     * Get trips categorized by their status (ongoing, past, upcoming)
     */
    public Map<String, List<Map<String, Object>>> getCategorizedTrips(String userIdentifier) {
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
            List<AcceptedTrip> allTrips = acceptedTripRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            
            // Initialize categories
            List<Map<String, Object>> ongoingTrips = new ArrayList<>();
            List<Map<String, Object>> pastTrips = new ArrayList<>();
            List<Map<String, Object>> upcomingTrips = new ArrayList<>();
            
            LocalDateTime now = LocalDateTime.now();
            
            for (AcceptedTrip trip : allTrips) {
                try {
                    Map<String, Object> tripData = new HashMap<>();
                    tripData.put("id", trip.getId());
                    tripData.put("createdAt", trip.getCreatedAt());
                    
                    Map<String, Object> tripPlan = objectMapper.readValue(trip.getTripPlan(), Map.class);
                    tripData.put("tripPlan", tripPlan);
                    
                    // Extract trip summary data
                    Map<String, Object> tripSummary = null;
                    String startDateStr = null;
                    Integer durationDays = null;
                    
                    if (tripPlan.containsKey("trip_summary")) {
                        tripSummary = (Map<String, Object>) tripPlan.get("trip_summary");
                        if (tripSummary != null) {
                            startDateStr = (String) tripSummary.get("start_date");
                            durationDays = (Integer) tripSummary.get("duration");
                        }
                    }
                    
                    // If no trip summary, try to get data from root level
                    if (startDateStr == null) {
                        startDateStr = (String) tripPlan.get("start_date");
                    }
                    if (durationDays == null) {
                        Object durationObj = tripPlan.get("duration_days");
                        if (durationObj instanceof Integer) {
                            durationDays = (Integer) durationObj;
                        } else if (durationObj instanceof Number) {
                            durationDays = ((Number) durationObj).intValue();
                        }
                    }
                    
                    // Categorize trip based on dates
                    if (startDateStr != null && durationDays != null) {
                        try {
                            LocalDateTime startDate = LocalDateTime.parse(startDateStr + "T00:00:00");
                            LocalDateTime endDate = startDate.plusDays(durationDays);
                            
                            if (now.isBefore(startDate)) {
                                // Trip hasn't started yet - upcoming
                                upcomingTrips.add(tripData);
                            } else if (now.isAfter(endDate)) {
                                // Trip has ended - past
                                pastTrips.add(tripData);
                            } else {
                                // Trip is currently active - ongoing
                                ongoingTrips.add(tripData);
                            }
                        } catch (Exception dateParseError) {
                            System.err.println("❌ Error parsing trip dates: " + dateParseError.getMessage());
                            // If we can't parse dates, categorize based on creation date (older than 7 days = past)
                            if (trip.getCreatedAt().isBefore(now.minusDays(7))) {
                                pastTrips.add(tripData);
                            } else {
                                upcomingTrips.add(tripData);
                            }
                        }
                    } else {
                        // If no date information available, categorize based on creation date
                        if (trip.getCreatedAt().isBefore(now.minusDays(7))) {
                            pastTrips.add(tripData);
                        } else {
                            upcomingTrips.add(tripData);
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Error processing trip: " + e.getMessage());
                    // Add to past trips as fallback
                    Map<String, Object> tripData = new HashMap<>();
                    tripData.put("id", trip.getId());
                    tripData.put("createdAt", trip.getCreatedAt());
                    tripData.put("tripPlan", null);
                    tripData.put("error", "Failed to parse trip plan");
                    pastTrips.add(tripData);
                }
            }
            
            Map<String, List<Map<String, Object>>> categorizedTrips = new HashMap<>();
            categorizedTrips.put("ongoing", ongoingTrips);
            categorizedTrips.put("past", pastTrips);
            categorizedTrips.put("upcoming", upcomingTrips);
            
            System.out.println("✅ Categorized trips - Ongoing: " + ongoingTrips.size() + 
                             ", Past: " + pastTrips.size() + 
                             ", Upcoming: " + upcomingTrips.size());
            
            return categorizedTrips;
            
        } catch (Exception e) {
            System.err.println("❌ Error categorizing trips: " + e.getMessage());
            throw new RuntimeException("Failed to categorize trips: " + e.getMessage());
        }
    }
}

package com.example.demo.service;

import com.example.demo.entity.AcceptedTrip;
import com.example.demo.entity.User;
import com.example.demo.Repository.AcceptedTripRepository;
import com.example.demo.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public boolean deleteAcceptedTrip(Long tripId, String userEmail) {
        try {
            Optional<AcceptedTrip> tripOptional = acceptedTripRepository.findById(tripId);
            if (!tripOptional.isPresent()) {
                return false;
            }

            AcceptedTrip trip = tripOptional.get();
            
            // Verify the trip belongs to the user
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            if (!userOptional.isPresent() || !trip.getUserId().equals(userOptional.get().getId())) {
                throw new RuntimeException("Unauthorized to delete this trip");
            }

            acceptedTripRepository.delete(trip);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting trip: " + e.getMessage());
            throw new RuntimeException("Failed to delete trip: " + e.getMessage());
        }
    }
}

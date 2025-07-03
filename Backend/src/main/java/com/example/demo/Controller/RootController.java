package com.example.demo.Controller;

import com.example.demo.service.AcceptedTripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RootController {

    @Autowired
    private AcceptedTripService acceptedTripService;

    /**
     * Root level save-trip endpoint (for compatibility with travel service)
     */
    @PostMapping("/save-trip")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> saveTrip(@RequestBody Map<String, Object> request, Authentication authentication) {
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
            System.err.println("❌ Error saving trip: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}

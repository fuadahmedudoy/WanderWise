package com.example.demo.Controller;

import com.example.demo.service.TripPlannerService;
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
    private TripPlannerService tripPlannerService;    @PostMapping("/plan")
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
}

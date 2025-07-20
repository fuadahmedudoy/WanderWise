package com.example.demo.Controller;

import com.example.demo.dto.*;
import com.example.demo.service.GroupTripService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/group-trips")
@RequiredArgsConstructor
public class GroupTripController {

    private final GroupTripService groupTripService;
    private final UserService userService;

    /**
     * Debug endpoint to check database tables
     */
    @GetMapping("/debug/tables")
    public ResponseEntity<?> debugTables() {
        try {
            long groupTripCount = groupTripService.getGroupTripCount();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "groupTripCount", groupTripCount,
                "message", "Database tables are accessible"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Database error: " + e.getMessage()
            ));
        }
    }

    /**
     * Create a new group trip
     */
    @PostMapping("/create")
    public ResponseEntity<?> createGroupTrip(@RequestBody CreateGroupTripRequest request, Authentication authentication) {
        try {
            // Get user ID
            String userIdentifier = authentication.getName();
            UUID userId = userService.getUserIdByEmailOrUsername(userIdentifier);
            
            // Basic validation
            if (request.getGroupName() == null || request.getGroupName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Group name is required"
                ));
            }

            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Description is required"
                ));
            }

            if (request.getMaxPeople() == null || request.getMaxPeople() < 2) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Maximum people must be at least 2"
                ));
            }

            System.out.println("DEBUG: All validations passed, user ID: " + userId);

            // Call service
            System.out.println("DEBUG: About to call groupTripService.createGroupTrip");
            ApiResponse<GroupTripResponse> response = groupTripService.createGroupTrip(request, userId);
            System.out.println("DEBUG: Service call completed, response: " + (response != null ? "received" : "null"));
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", response.getMessage(),
                    "data", response.getData()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", response.getError()
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to create group trip: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all available group trips (excluding user's own trips)
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableGroupTrips(Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            UUID userId = userService.getUserIdByEmailOrUsername(userIdentifier);

            ApiResponse<List<GroupTripResponse>> response = groupTripService.getAllAvailableGroupTrips(userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response.getData()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", response.getError()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to fetch available group trips: " + e.getMessage()
            ));
        }
    }

    /**
     * Get user's own group trips
     */
    @GetMapping("/my-trips")
    public ResponseEntity<?> getMyGroupTrips(Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            UUID userId = userService.getUserIdByEmailOrUsername(userIdentifier);

            ApiResponse<List<GroupTripResponse>> response = groupTripService.getMyGroupTrips(userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response.getData()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", response.getError()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to fetch your group trips: " + e.getMessage()
            ));
        }
    }

    /**
     * Join a group trip
     */
    @PostMapping("/{groupTripId}/join")
    public ResponseEntity<?> joinGroupTrip(@PathVariable UUID groupTripId, 
                                         @RequestBody JoinGroupTripRequest request, 
                                         Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            UUID userId = userService.getUserIdByEmailOrUsername(userIdentifier);

            ApiResponse<String> response = groupTripService.joinGroupTrip(groupTripId, request, userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", response.getMessage()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", response.getError()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to join group trip: " + e.getMessage()
            ));
        }
    }

    /**
     * Approve or reject a join request (for group trip creators)
     */
    @PostMapping("/{groupTripId}/members/{memberId}/respond")
    public ResponseEntity<?> respondToJoinRequest(@PathVariable UUID groupTripId,
                                                @PathVariable UUID memberId,
                                                @RequestParam boolean approve,
                                                Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            UUID userId = userService.getUserIdByEmailOrUsername(userIdentifier);

            ApiResponse<String> response = groupTripService.respondToJoinRequest(groupTripId, memberId, approve, userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", response.getMessage()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", response.getError()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to respond to join request: " + e.getMessage()
            ));
        }
    }

    /**
     * Get detailed information about a specific group trip
     */
    @GetMapping("/{groupTripId}")
    public ResponseEntity<?> getGroupTripDetails(@PathVariable UUID groupTripId, Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            UUID userId = userService.getUserIdByEmailOrUsername(userIdentifier);

            ApiResponse<GroupTripResponse> response = groupTripService.getGroupTripDetails(groupTripId, userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response.getData()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", response.getError()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to fetch group trip details: " + e.getMessage()
            ));
        }
    }

    /**
     * Get group chat messages for a specific group trip
     */
    @GetMapping("/{groupTripId}/chat")
    public ResponseEntity<?> getGroupChatMessages(@PathVariable UUID groupTripId, Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            UUID userId = userService.getUserIdByEmailOrUsername(userIdentifier);

            ApiResponse<List<GroupChatMessageResponse>> response = groupTripService.getGroupChatMessages(groupTripId, userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response.getData()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", response.getError()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to fetch chat messages: " + e.getMessage()
            ));
        }
    }

    /**
     * Send a message to group chat
     */
    @PostMapping("/{groupTripId}/chat")
    public ResponseEntity<?> sendGroupChatMessage(@PathVariable UUID groupTripId, 
                                                 @RequestBody Map<String, String> request,
                                                 Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            UUID userId = userService.getUserIdByEmailOrUsername(userIdentifier);
            String message = request.get("message");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Message cannot be empty"
                ));
            }

            ApiResponse<GroupChatMessageResponse> response = groupTripService.sendGroupChatMessage(groupTripId, message, userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response.getData()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", response.getError()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to send message: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all members of a group trip (for trip creator to manage approval requests)
     */
    @GetMapping("/{groupTripId}/members")
    public ResponseEntity<?> getGroupTripMembers(@PathVariable UUID groupTripId, Authentication authentication) {
        try {
            String userIdentifier = authentication.getName();
            UUID userId = userService.getUserIdByEmailOrUsername(userIdentifier);

            ApiResponse<List<GroupTripMemberResponse>> response = groupTripService.getGroupTripMembers(groupTripId, userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response.getData()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", response.getError()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to fetch group members: " + e.getMessage()
            ));
        }
    }
}

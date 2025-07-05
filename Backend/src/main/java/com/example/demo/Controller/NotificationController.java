package com.example.demo.Controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(@AuthenticationPrincipal User user) {
        System.out.println("🔍 GET /api/notifications endpoint called");
        
        try {
            if (user == null) {
                System.err.println("❌ User is null in getNotifications - returning 401");
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "User not authenticated"));
            }
            
            System.out.println("✅ Getting notifications for user: " + user.getEmail() + " (ID: " + user.getId() + ")");
            
            // Test database connection first
            System.out.println("🔍 Testing notification repository...");
            List<Notification> notifications = notificationService.getUserNotifications(user.getId());
            System.out.println("✅ Retrieved " + notifications.size() + " notifications from database");
            
            long unreadCount = notificationService.getUnreadCount(user.getId());
            System.out.println("✅ Unread count: " + unreadCount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", notifications);
            response.put("unreadCount", unreadCount);

            System.out.println("✅ Returning successful response with " + notifications.size() + " notifications");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error in getNotifications: " + e.getMessage());
            System.err.println("❌ Error class: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                System.err.println("❌ User is null in getUnreadNotifications");
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "User not authenticated"));
            }
            
            List<Notification> notifications = notificationService.getUnreadNotifications(user.getId());
            long unreadCount = notifications.size();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "notifications", notifications,
                "unreadCount", unreadCount
            ));
        } catch (Exception e) {
            System.err.println("❌ Error in getUnreadNotifications: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable UUID id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                System.err.println("❌ User is null in markAllAsRead");
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "User not authenticated"));
            }
            
            notificationService.markAllAsRead(user.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read"));
        } catch (Exception e) {
            System.err.println("❌ Error in markAllAsRead: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable UUID id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/user/{userId}/all")
    public ResponseEntity<Map<String, Object>> clearAllNotifications(@PathVariable UUID userId) {
        try {
            notificationService.clearAllNotifications(userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "All notifications cleared successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Simple test endpoint to verify API is working
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        System.out.println("🧪 TEST ENDPOINT CALLED - API is working!");
        return ResponseEntity.ok(Map.of(
            "success", true, 
            "message", "Notification API is working!",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
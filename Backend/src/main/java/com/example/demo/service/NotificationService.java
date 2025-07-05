package com.example.demo.service;

import com.example.demo.Repository.NotificationRepository;
import com.example.demo.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = false)
    public void sendWeatherAlert(UUID userId, String destination, String weatherInfo, Long tripId) {
        try {
            System.out.println("🔍 Starting notification save process for user: " + userId);
            
            // Save notification to database
            Notification notification = Notification.builder()
                    .userId(userId)
                    .title("⚠️ Weather Alert for " + destination)
                    .message("Adverse weather conditions detected for your upcoming trip to " + destination + ". " + weatherInfo)
                    .type(Notification.NotificationType.WEATHER_ALERT)
                    .tripId(tripId)
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build();

            System.out.println("🔍 Notification object created: " + notification);
            
            Notification saved = notificationRepository.save(notification);
            notificationRepository.flush(); // Force immediate database write
            System.out.println("✅ Notification saved successfully with ID: " + saved.getId());
            System.out.println("🔍 Saved notification details: " + saved);
            
            // Verify the notification was actually written to the database
            System.out.println("🔍 Verifying notification in database...");
            List<Notification> allUserNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
            System.out.println("📋 Total notifications for user " + userId + ": " + allUserNotifications.size());
            
            // Check if our notification exists
            boolean found = allUserNotifications.stream()
                    .anyMatch(n -> n.getId().equals(saved.getId()));
            System.out.println("✅ Notification exists in database: " + found);
            
            if (found) {
                System.out.println("🎉 SUCCESS: Notification was successfully written to database!");
                System.out.println("📄 Notification ID: " + saved.getId());
                System.out.println("👤 User ID: " + saved.getUserId());
                System.out.println("📝 Title: " + saved.getTitle());
                System.out.println("📅 Created At: " + saved.getCreatedAt());
                System.out.println("🔄 Is Read: " + saved.isRead());
            } else {
                System.err.println("❌ FAILED: Notification was NOT found in database after save!");
            }

            // Send real-time notification via WebSocket
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("id", saved.getId());
            notificationData.put("title", saved.getTitle());
            notificationData.put("message", saved.getMessage());
            notificationData.put("type", saved.getType());
            notificationData.put("timestamp", saved.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            notificationData.put("tripId", saved.getTripId());
            notificationData.put("isRead", false);

            // Send to specific user
            messagingTemplate.convertAndSendToUser(
                    userId.toString(), 
                    "/queue/notifications", 
                    notificationData
            );

            System.out.println("📱 Real-time notification sent to user: " + userId);
            System.out.println("🌩️ Weather alert: " + destination);
            
        } catch (Exception e) {
            System.err.println("❌ Error saving notification: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save notification", e);
        }
    }

    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public void clearAllNotifications(UUID userId) {
        List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(userNotifications);
    }

    // Test method to verify notification saving
    @Transactional
    public void testNotificationSave(UUID userId) {
        try {
            System.out.println("🧪 Testing notification save for user: " + userId);
            
            Notification testNotification = Notification.builder()
                    .userId(userId)
                    .title("Test Notification")
                    .message("This is a test notification")
                    .type(Notification.NotificationType.SYSTEM_ALERT)
                    .build();
            
            System.out.println("🔍 Test notification before save: " + testNotification);
            
            Notification saved = notificationRepository.save(testNotification);
            System.out.println("✅ Test notification saved with ID: " + saved.getId());
            
            // Verify it was saved
            List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
            System.out.println("📋 Total notifications for user: " + userNotifications.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error in test notification save: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
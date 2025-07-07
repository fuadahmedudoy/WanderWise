package com.example.demo.service;

import com.example.demo.Repository.NotificationRepository;
import com.example.demo.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private UUID testUserId;
    private UUID testNotificationId;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNotificationId = UUID.randomUUID();
        
        testNotification = Notification.builder()
                .id(testNotificationId)
                .userId(testUserId)
                .title("Test Notification")
                .message("This is a test notification")
                .type(Notification.NotificationType.WEATHER_ALERT)
                .tripId(1L)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    @Test
    void testSendWeatherAlert_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(Arrays.asList(testNotification));

        // Act
        notificationService.sendWeatherAlert(testUserId, "Dhaka", "Heavy rain expected", 1L);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        verify(notificationRepository).flush();
        
        Notification savedNotification = notificationCaptor.getValue();
        assertEquals(testUserId, savedNotification.getUserId());
        assertEquals("⚠️ Weather Alert for Dhaka", savedNotification.getTitle());
        assertTrue(savedNotification.getMessage().contains("Heavy rain expected"));
        assertEquals(Notification.NotificationType.WEATHER_ALERT, savedNotification.getType());
        assertEquals(1L, savedNotification.getTripId());
        assertFalse(savedNotification.isRead());

        // Verify WebSocket message sent
        verify(messagingTemplate).convertAndSendToUser(
                eq(testUserId.toString()),
                eq("/queue/notifications"),
                any()
        );
    }

    @Test
    void testGetUserNotifications_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUserNotifications(testUserId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testNotification, result.get(0));
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(testUserId);
    }

    @Test
    void testGetUnreadNotifications_Success() {
        // Arrange
        List<Notification> unreadNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(testUserId))
                .thenReturn(unreadNotifications);

        // Act
        List<Notification> result = notificationService.getUnreadNotifications(testUserId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testNotification, result.get(0));
        verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(testUserId);
    }

    @Test
    void testGetUnreadCount_Success() {
        // Arrange
        when(notificationRepository.countByUserIdAndIsReadFalse(testUserId)).thenReturn(3L);

        // Act
        long count = notificationService.getUnreadCount(testUserId);

        // Assert
        assertEquals(3L, count);
        verify(notificationRepository).countByUserIdAndIsReadFalse(testUserId);
    }

    @Test
    void testMarkAsRead_Success() {
        // Arrange
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.of(testNotification));

        // Act
        notificationService.markAsRead(testNotificationId);

        // Assert
        verify(notificationRepository).findById(testNotificationId);
        verify(notificationRepository).save(testNotification);
        assertTrue(testNotification.isRead());
    }

    @Test
    void testMarkAsRead_NotificationNotFound() {
        // Arrange
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.empty());

        // Act
        notificationService.markAsRead(testNotificationId);

        // Assert
        verify(notificationRepository).findById(testNotificationId);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAllAsRead_Success() {
        // Arrange
        List<Notification> unreadNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(testUserId))
                .thenReturn(unreadNotifications);

        // Act
        notificationService.markAllAsRead(testUserId);

        // Assert
        verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(testUserId);
        verify(notificationRepository).saveAll(unreadNotifications);
        assertTrue(testNotification.isRead());
    }

    @Test
    void testDeleteNotification_Success() {
        // Act
        notificationService.deleteNotification(testNotificationId);

        // Assert
        verify(notificationRepository).deleteById(testNotificationId);
    }

    @Test
    void testClearAllNotifications_Success() {
        // Arrange
        List<Notification> userNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(userNotifications);

        // Act
        notificationService.clearAllNotifications(testUserId);

        // Assert
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(testUserId);
        verify(notificationRepository).deleteAll(userNotifications);
    }

    @Test
    void testTestNotificationSave_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(Arrays.asList(testNotification));

        // Act
        notificationService.testNotificationSave(testUserId);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(testUserId);
        
        Notification savedNotification = notificationCaptor.getValue();
        assertEquals(testUserId, savedNotification.getUserId());
        assertEquals("Test Notification", savedNotification.getTitle());
        assertEquals("This is a test notification", savedNotification.getMessage());
        assertEquals(Notification.NotificationType.SYSTEM_ALERT, savedNotification.getType());
    }

    @Test
    void testSendWeatherAlert_ExceptionHandling() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.sendWeatherAlert(testUserId, "Dhaka", "Heavy rain expected", 1L);
        });

        assertEquals("Failed to save notification", exception.getMessage());
        verify(notificationRepository).save(any(Notification.class));
    }
}

package com.example.demo.User;

import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private UserProfile userProfile;

    @BeforeEach
    void setUp() {
        user = new User();
        userProfile = new UserProfile();
    }

    @Test
    void testUserCreation() {
        // Arrange
        UUID userId = UUID.fromString("10291323-21ab-4cde-8fab-1234567890ab");

        // Act
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole("USER");

        // Assert
        assertEquals(userId, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("USER", user.getRole());
    }

    @Test
    void testUserProfileRelationship() {
        // Arrange
        userProfile.setUser(user);
        user.setUserProfile(userProfile);

        // Act & Assert
        assertNotNull(user.getUserProfile());
        assertEquals(userProfile, user.getUserProfile());
        assertEquals(user, userProfile.getUser());
    }

    @Test
    void testUserEquality() {
        // Arrange
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        User user1 = new User();
        user1.setId(uuid1);
        user1.setEmail("test@example.com");

        User user2 = new User();
        user2.setId(uuid1);
        user2.setEmail("test@example.com");

        User user3 = new User();
        user3.setId(uuid2);
        user3.setEmail("different@example.com");

        // Act & Assert
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getEmail(), user2.getEmail());
        assertNotEquals(user1.getId(), user3.getId());
    }

    @Test
    void testUserDefaultValues() {
        // Act & Assert
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getRole());
        assertNull(user.getUserProfile());
    }
}

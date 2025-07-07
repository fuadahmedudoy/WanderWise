package com.example.demo.service;

import com.example.demo.Repository.AcceptedTripRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.entity.AcceptedTrip;
import com.example.demo.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcceptedTripServiceTest {

    @Mock
    private AcceptedTripRepository acceptedTripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AcceptedTripService acceptedTripService;

    private User testUser;
    private AcceptedTrip testTrip;
    private Map<String, Object> testTripPlan;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testTrip = AcceptedTrip.builder()
                .id(1L)
                .userId(testUser.getId())
                .tripPlan("{\"destination\":\"Dhaka\"}")
                .createdAt(LocalDateTime.now())
                .build();

        testTripPlan = new HashMap<>();
        Map<String, Object> tripSummary = new HashMap<>();
        tripSummary.put("destination", "Dhaka");
        tripSummary.put("duration", 3);
        tripSummary.put("total_budget", 25000.0);
        tripSummary.put("start_date", "2025-08-01");
        tripSummary.put("origin", "Chittagong");
        testTripPlan.put("trip_summary", tripSummary);
    }

    @Test
    void testAcceptTrip_Success() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(testTripPlan)).thenReturn("{\"destination\":\"Dhaka\"}");
        when(acceptedTripRepository.save(any(AcceptedTrip.class))).thenReturn(testTrip);

        // Act
        AcceptedTrip result = acceptedTripService.acceptTrip("testuser", testTripPlan);

        // Assert
        assertNotNull(result);
        assertEquals(testTrip.getId(), result.getId());
        assertEquals(testUser.getId(), result.getUserId());

        ArgumentCaptor<AcceptedTrip> tripCaptor = ArgumentCaptor.forClass(AcceptedTrip.class);
        verify(acceptedTripRepository).save(tripCaptor.capture());
        
        AcceptedTrip savedTrip = tripCaptor.getValue();
        assertEquals(testUser.getId(), savedTrip.getUserId());
        assertEquals("{\"destination\":\"Dhaka\"}", savedTrip.getTripPlan());
    }

    @Test
    void testAcceptTrip_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            acceptedTripService.acceptTrip("nonexistent", testTripPlan);
        });

        assertEquals("Failed to accept trip: User not found with identifier: nonexistent", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository).findByEmail("nonexistent");
    }

    @Test
    void testAcceptTrip_FindByEmail() throws Exception {
        // Arrange
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(testTripPlan)).thenReturn("{\"destination\":\"Dhaka\"}");
        when(acceptedTripRepository.save(any(AcceptedTrip.class))).thenReturn(testTrip);

        // Act
        AcceptedTrip result = acceptedTripService.acceptTrip("test@example.com", testTripPlan);

        // Assert
        assertNotNull(result);
        verify(userRepository).findByUsername("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        verify(acceptedTripRepository).save(any(AcceptedTrip.class));
    }

    @Test
    void testGetUserAcceptedTrips_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(acceptedTripRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(Arrays.asList(testTrip));

        // Act
        List<AcceptedTrip> result = acceptedTripService.getUserAcceptedTrips("testuser");

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTrip, result.get(0));
        verify(acceptedTripRepository).findByUserIdOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    void testGetUserAcceptedTrips_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            acceptedTripService.getUserAcceptedTrips("nonexistent");
        });

        assertEquals("Failed to fetch user trips: User not found with identifier: nonexistent", exception.getMessage());
    }

    @Test
    void testGetAcceptedTripById_Success() {
        // Arrange
        when(acceptedTripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        // Act
        Optional<AcceptedTrip> result = acceptedTripService.getAcceptedTripById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTrip, result.get());
        verify(acceptedTripRepository).findById(1L);
    }

    @Test
    void testGetAcceptedTripById_NotFound() {
        // Arrange
        when(acceptedTripRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<AcceptedTrip> result = acceptedTripService.getAcceptedTripById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(acceptedTripRepository).findById(1L);
    }

    @Test
    void testGetRecentAcceptedTrips_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(acceptedTripRepository.findRecentTripsByUserId(eq(testUser.getId()), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testTrip));

        // Act
        List<AcceptedTrip> result = acceptedTripService.getRecentAcceptedTrips("test@example.com");

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTrip, result.get(0));
        verify(acceptedTripRepository).findRecentTripsByUserId(eq(testUser.getId()), any(LocalDateTime.class));
    }

    @Test
    void testGetRecentAcceptedTrips_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            acceptedTripService.getRecentAcceptedTrips("nonexistent@example.com");
        });

        assertEquals("Failed to fetch recent trips: User not found with email: nonexistent@example.com", exception.getMessage());
    }

    @Test
    void testCountUserAcceptedTrips_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(acceptedTripRepository.countByUserId(testUser.getId())).thenReturn(5L);

        // Act
        long count = acceptedTripService.countUserAcceptedTrips("test@example.com");

        // Assert
        assertEquals(5L, count);
        verify(acceptedTripRepository).countByUserId(testUser.getId());
    }

    @Test
    void testCountUserAcceptedTrips_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        long count = acceptedTripService.countUserAcceptedTrips("nonexistent@example.com");

        // Assert
        assertEquals(0L, count);
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void testDeleteAcceptedTrip_Success() {
        // Arrange
        when(acceptedTripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        boolean result = acceptedTripService.deleteAcceptedTrip(1L, "testuser");

        // Assert
        assertTrue(result);
        verify(acceptedTripRepository).findById(1L);
        verify(acceptedTripRepository).delete(testTrip);
    }

    @Test
    void testDeleteAcceptedTrip_TripNotFound() {
        // Arrange
        when(acceptedTripRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = acceptedTripService.deleteAcceptedTrip(1L, "testuser");

        // Assert
        assertFalse(result);
        verify(acceptedTripRepository).findById(1L);
        verify(acceptedTripRepository, never()).delete(any());
    }

    @Test
    void testDeleteAcceptedTrip_UnauthorizedUser() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("anotheruser");
        
        when(acceptedTripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            acceptedTripService.deleteAcceptedTrip(1L, "anotheruser");
        });

        assertEquals("Failed to delete trip: Unauthorized to delete this trip", exception.getMessage());
        verify(acceptedTripRepository, never()).delete(any());
    }

    @Test
    void testGetCategorizedTrips_Success() throws Exception {
        // Arrange
        String tripPlanJson = "{\"trip_summary\":{\"destination\":\"Dhaka\",\"start_date\":\"2025-12-01\",\"duration\":3}}";
        testTrip.setTripPlan(tripPlanJson);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(acceptedTripRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(Arrays.asList(testTrip));
        
        Map<String, Object> parsedTripPlan = new HashMap<>();
        Map<String, Object> tripSummary = new HashMap<>();
        tripSummary.put("destination", "Dhaka");
        tripSummary.put("start_date", "2025-12-01");
        tripSummary.put("duration", 3);
        parsedTripPlan.put("trip_summary", tripSummary);
        
        when(objectMapper.readValue(tripPlanJson, Map.class)).thenReturn(parsedTripPlan);

        // Act
        Map<String, List<Map<String, Object>>> result = acceptedTripService.getCategorizedTrips("testuser");

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("ongoing"));
        assertTrue(result.containsKey("past"));
        assertTrue(result.containsKey("upcoming"));
        
        // One of the categories should contain our trip
        int totalTrips = result.get("ongoing").size() + result.get("past").size() + result.get("upcoming").size();
        assertEquals(1, totalTrips);
    }

    @Test
    void testGetCategorizedTrips_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            acceptedTripService.getCategorizedTrips("nonexistent");
        });

        assertEquals("Failed to categorize trips: User not found with identifier: nonexistent", exception.getMessage());
    }

    @Test
    void testAcceptTrip_JsonProcessingException() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(testTripPlan)).thenThrow(new RuntimeException("JSON error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            acceptedTripService.acceptTrip("testuser", testTripPlan);
        });

        assertEquals("Failed to accept trip: JSON error", exception.getMessage());
    }
}

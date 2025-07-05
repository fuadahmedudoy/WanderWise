package com.example.demo.service;

import com.example.demo.dto.TripPlanRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripPlannerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CityDataService cityDataService;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    @InjectMocks
    private TripPlannerService tripPlannerService;

    private TripPlanRequest tripPlanRequest;
    private Map<String, Object> mockCityData;
    private Map<String, Object> mockPythonResponse;

    @BeforeEach
    void setUp() throws Exception {
        // Set the python service URL for testing
        ReflectionTestUtils.setField(tripPlannerService, "pythonServiceUrl", "http://localhost:5001");
        
        tripPlanRequest = new TripPlanRequest();
        tripPlanRequest.setDestination("Sylhet");
        tripPlanRequest.setStartDate(LocalDate.now().plusDays(7).toString());
        tripPlanRequest.setDurationDays(3);
        tripPlanRequest.setBudget(25000.0);
        tripPlanRequest.setOrigin("Dhaka");

        mockCityData = new HashMap<>();
        mockCityData.put("success", true);
        mockCityData.put("city", Map.of("id", 1, "name", "Sylhet", "description", "Tea city"));

        mockPythonResponse = new HashMap<>();
        mockPythonResponse.put("success", true);
        mockPythonResponse.put("trip_plan", "Mock itinerary");

        // Use lenient stubbing for database connection mocks since they're not used in all tests
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.getMetaData()).thenReturn(metaData);
        lenient().when(metaData.getURL()).thenReturn("jdbc:postgresql://localhost:5432/wanderwise");
        lenient().when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        lenient().when(metaData.getDatabaseProductVersion()).thenReturn("13.0");
    }

    @Test
    void testPlanTrip_Success() {
        // Arrange
        String userEmail = "test@example.com";
        when(cityDataService.getCityData("Sylhet", userEmail)).thenReturn(mockCityData);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(mockPythonResponse, HttpStatus.OK));

        // Act
        Map<String, Object> result = tripPlannerService.planTrip(tripPlanRequest, userEmail);

        // Assert
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Mock itinerary", result.get("trip_plan"));
        assertEquals("spring-boot-gateway", result.get("processed_by"));
        assertEquals(userEmail, result.get("user_email"));
        
        verify(cityDataService).getCityData("Sylhet", userEmail);
        verify(restTemplate).exchange(
                eq("http://localhost:5001/plan-trip"), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        );
    }

    @Test
    void testPlanTrip_CityDataNull() {
        // Arrange
        String userEmail = "test@example.com";
        when(cityDataService.getCityData("Sylhet", userEmail)).thenReturn(null);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "City data not available");
        
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(errorResponse, HttpStatus.OK));

        // Act
        Map<String, Object> result = tripPlannerService.planTrip(tripPlanRequest, userEmail);

        // Assert
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("City data not available", result.get("error"));
        
        verify(cityDataService).getCityData("Sylhet", userEmail);
        verify(restTemplate).exchange(
                anyString(), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        );
    }

    @Test
    void testPlanTrip_PythonServiceError() {
        // Arrange
        String userEmail = "test@example.com";
        when(cityDataService.getCityData("Sylhet", userEmail)).thenReturn(mockCityData);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        )).thenThrow(new RuntimeException("Python service unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> tripPlannerService.planTrip(tripPlanRequest, userEmail));
        
        assertTrue(exception.getMessage().contains("Failed to plan trip"));
        assertTrue(exception.getMessage().contains("Failed to communicate with Python service"));
        
        verify(cityDataService).getCityData("Sylhet", userEmail);
        verify(restTemplate).exchange(
                anyString(), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        );
    }

    @Test
    void testPlanTrip_DatabaseConnectionError() throws Exception {
        // Arrange
        String userEmail = "test@example.com";
        // Override the lenient stub for this specific test
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Database connection failed"));
        when(cityDataService.getCityData("Sylhet", userEmail)).thenReturn(mockCityData);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(mockPythonResponse, HttpStatus.OK));

        // Act
        Map<String, Object> result = tripPlannerService.planTrip(tripPlanRequest, userEmail);

        // Assert
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        // The service should continue working even if database connection debug fails
        
        verify(cityDataService).getCityData("Sylhet", userEmail);
        verify(restTemplate).exchange(
                anyString(), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        );
    }

    @Test
    void testPlanTrip_NullResponse() {
        // Arrange
        String userEmail = "test@example.com";
        when(cityDataService.getCityData("Sylhet", userEmail)).thenReturn(mockCityData);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> tripPlannerService.planTrip(tripPlanRequest, userEmail));
        
        assertTrue(exception.getMessage().contains("Failed to plan trip"));
        
        verify(cityDataService).getCityData("Sylhet", userEmail);
        verify(restTemplate).exchange(
                anyString(), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(Map.class)
        );
    }
}
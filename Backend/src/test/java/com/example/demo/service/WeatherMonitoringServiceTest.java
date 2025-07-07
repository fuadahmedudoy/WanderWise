package com.example.demo.service;

import com.example.demo.Repository.TripPlanRepository;
import com.example.demo.entity.TripPlan;
import com.example.demo.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherMonitoringServiceTest {

    @Mock
    private TripPlanRepository tripPlanRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WeatherMonitoringService weatherMonitoringService;

    private TripPlan testTripPlan;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");

        String tripPlanJson = """
            {
                "trip_summary": {
                    "destination": "Dhaka",
                    "origin": "Chittagong",
                    "start_date": "2025-07-09",
                    "duration": 3,
                    "total_budget": 25000.0
                }
            }
            """;

        testTripPlan = TripPlan.builder()
                .id(1L)
                .userId(testUser.getId())
                .tripPlan(tripPlanJson)
                .status(TripPlan.TripStatus.UPCOMING)
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();
    }

    @Test
    void testManualWeatherCheck_Success() {
        // Arrange
        when(tripPlanRepository.findUpcomingTripsInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testTripPlan));

        // Mock weather API response
        String mockWeatherResponse = "{"
                + "\"forecast\": {"
                + "\"forecastday\": [{"
                + "\"date\": \"2025-07-10\","
                + "\"day\": {"
                + "\"condition\": {\"text\": \"Sunny\"},"
                + "\"avgtemp_c\": 30.0,"
                + "\"totalprecip_mm\": 0.0,"
                + "\"maxwind_kph\": 10.0"
                + "}}]}}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockWeatherResponse);

        // Mock ObjectMapper
        try {
            JsonNode mockRootNode = mock(JsonNode.class);
            JsonNode mockForecastNode = mock(JsonNode.class);
            JsonNode mockForecastDayNode = mock(JsonNode.class);
            JsonNode mockDayNode = mock(JsonNode.class);
            JsonNode mockConditionNode = mock(JsonNode.class);
            JsonNode mockDateNode = mock(JsonNode.class);
            JsonNode mockTempNode = mock(JsonNode.class);
            JsonNode mockPrecipNode = mock(JsonNode.class);
            JsonNode mockWindNode = mock(JsonNode.class);

            when(objectMapper.readTree(mockWeatherResponse)).thenReturn(mockRootNode);
            when(mockRootNode.path("forecast")).thenReturn(mockForecastNode);
            when(mockForecastNode.path("forecastday")).thenReturn(mockForecastDayNode);
            when(mockForecastDayNode.iterator()).thenReturn(Arrays.asList(mockDayNode).iterator());
            
            when(mockDayNode.get("date")).thenReturn(mockDateNode);
            when(mockDateNode.asText()).thenReturn("2025-07-10");
            
            when(mockDayNode.path("day")).thenReturn(mockDayNode);
            when(mockDayNode.path("condition")).thenReturn(mockConditionNode);
            when(mockConditionNode.path("text")).thenReturn(mockConditionNode);
            when(mockConditionNode.asText()).thenReturn("Sunny");
            
            when(mockDayNode.path("avgtemp_c")).thenReturn(mockTempNode);
            when(mockTempNode.asDouble()).thenReturn(30.0);
            
            when(mockDayNode.path("totalprecip_mm")).thenReturn(mockPrecipNode);
            when(mockPrecipNode.asDouble()).thenReturn(0.0);
            
            when(mockDayNode.path("maxwind_kph")).thenReturn(mockWindNode);
            when(mockWindNode.asDouble()).thenReturn(10.0);

        } catch (Exception e) {
            fail("Failed to mock ObjectMapper: " + e.getMessage());
        }

        // Act
        weatherMonitoringService.manualWeatherCheck();

        // Assert
        verify(tripPlanRepository).findUpcomingTripsInRange(any(LocalDate.class), any(LocalDate.class));
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void testGetWeatherDetailsForTrip_Success() {
        // Arrange
        when(tripPlanRepository.findById(1L)).thenReturn(Optional.of(testTripPlan));

        // Mock weather API response
        String mockWeatherResponse = "{"
                + "\"forecast\": {"
                + "\"forecastday\": [{"
                + "\"date\": \"2025-07-10\","
                + "\"day\": {"
                + "\"condition\": {\"text\": \"Sunny\"},"
                + "\"avgtemp_c\": 30.0,"
                + "\"totalprecip_mm\": 0.0,"
                + "\"maxwind_kph\": 10.0"
                + "}}]}}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockWeatherResponse);

        // Mock ObjectMapper
        try {
            JsonNode mockRootNode = mock(JsonNode.class);
            JsonNode mockForecastNode = mock(JsonNode.class);
            JsonNode mockForecastDayNode = mock(JsonNode.class);
            JsonNode mockDayNode = mock(JsonNode.class);
            JsonNode mockConditionNode = mock(JsonNode.class);
            JsonNode mockDateNode = mock(JsonNode.class);
            JsonNode mockTempNode = mock(JsonNode.class);
            JsonNode mockPrecipNode = mock(JsonNode.class);
            JsonNode mockWindNode = mock(JsonNode.class);

            when(objectMapper.readTree(mockWeatherResponse)).thenReturn(mockRootNode);
            when(mockRootNode.path("forecast")).thenReturn(mockForecastNode);
            when(mockForecastNode.path("forecastday")).thenReturn(mockForecastDayNode);
            when(mockForecastDayNode.iterator()).thenReturn(Arrays.asList(mockDayNode).iterator());
            
            when(mockDayNode.get("date")).thenReturn(mockDateNode);
            when(mockDateNode.asText()).thenReturn("2025-07-10");
            
            when(mockDayNode.path("day")).thenReturn(mockDayNode);
            when(mockDayNode.path("condition")).thenReturn(mockConditionNode);
            when(mockConditionNode.path("text")).thenReturn(mockConditionNode);
            when(mockConditionNode.asText()).thenReturn("Sunny");
            
            when(mockDayNode.path("avgtemp_c")).thenReturn(mockTempNode);
            when(mockTempNode.asDouble()).thenReturn(30.0);
            
            when(mockDayNode.path("totalprecip_mm")).thenReturn(mockPrecipNode);
            when(mockPrecipNode.asDouble()).thenReturn(0.0);
            
            when(mockDayNode.path("maxwind_kph")).thenReturn(mockWindNode);
            when(mockWindNode.asDouble()).thenReturn(10.0);

        } catch (Exception e) {
            fail("Failed to mock ObjectMapper: " + e.getMessage());
        }

        // Act
        Map<String, Object> result = weatherMonitoringService.getWeatherDetailsForTrip(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Dhaka", result.get("location"));
        assertEquals(testTripPlan.getStartDate(), result.get("tripStartDate"));
        assertTrue(result.containsKey("forecast"));
        assertTrue(result.containsKey("alerts"));
        
        verify(tripPlanRepository).findById(1L);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void testGetWeatherDetailsForTrip_TripNotFound() {
        // Arrange
        when(tripPlanRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            weatherMonitoringService.getWeatherDetailsForTrip(1L);
        });

        assertEquals("Trip not found", exception.getMessage());
        verify(tripPlanRepository).findById(1L);
    }

    @Test
    void testGetWeatherDetailsForTrip_WeatherAPIFailure() {
        // Arrange
        when(tripPlanRepository.findById(1L)).thenReturn(Optional.of(testTripPlan));
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Weather API error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            weatherMonitoringService.getWeatherDetailsForTrip(1L);
        });

        assertEquals("Failed to fetch weather data", exception.getMessage());
        verify(tripPlanRepository).findById(1L);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

//    @Test
//    void testGetWeatherDetailsForTrip_Success() {
//        // Arrange
//        String mockWeatherResponse = "{"
//                + "\"forecast\": {"
//                + "\"forecastday\": [{"
//                + "\"day\": {"
//                + "\"condition\": {\"text\": \"Sunny\"},"
//                + "\"maxtemp_c\": 32.0,"
//                + "\"mintemp_c\": 22.0,"
//                + "\"maxwind_kph\": 15.0,"
//                + "\"avgtemp_c\": 27.0,"
//                + "\"totalprecip_mm\": 0.0"
//                + "}}]}}";
//
//        when(tripPlanRepository.findById(1L)).thenReturn(Optional.of(testTripPlan));
//        when(restTemplate.getForObject(anyString(), eq(String.class)))
//                .thenReturn(mockWeatherResponse);
//
//        // Act
//        Map<String, Object> result = weatherMonitoringService.getWeatherDetailsForTrip(1L);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("Dhaka", result.get("location"));
//        assertTrue(result.containsKey("forecast"));
//        verify(tripPlanRepository).findById(1L);
//        verify(restTemplate).getForObject(anyString(), eq(String.class));
//    }

//    @Test
//    void testGetWeatherDetailsForTrip_TripNotFound() {
//        // Arrange
//        when(tripPlanRepository.findById(1L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            weatherMonitoringService.getWeatherDetailsForTrip(1L);
//        });
//
//        assertEquals("Trip not found", exception.getMessage());
//        verify(tripPlanRepository).findById(1L);
//    }

    @Test
    void testGetWeatherDetailsForTrip_WeatherDataFetchFailure() {
        // Arrange
        when(tripPlanRepository.findById(1L)).thenReturn(Optional.of(testTripPlan));
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            weatherMonitoringService.getWeatherDetailsForTrip(1L);
        });

        assertEquals("Failed to fetch weather data", exception.getMessage());
        verify(tripPlanRepository).findById(1L);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void testScheduleWeatherCheck_NoUpcomingTrips() {
        // Arrange
        when(tripPlanRepository.findUpcomingTripsInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(tripPlanRepository.findByStatus(TripPlan.TripStatus.UPCOMING))
                .thenReturn(Collections.emptyList());

        // Act
        weatherMonitoringService.scheduleWeatherCheck();

        // Assert
        verify(tripPlanRepository).findUpcomingTripsInRange(any(LocalDate.class), any(LocalDate.class));
        verify(tripPlanRepository).findByStatus(TripPlan.TripStatus.UPCOMING);
    }

    @Test
    void testScheduleWeatherCheck_WithUpcomingTrips() {
        // Arrange
        when(tripPlanRepository.findUpcomingTripsInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testTripPlan));

        // Mock weather API response for good weather
        String mockWeatherResponse = "{"
                + "\"forecast\": {"
                + "\"forecastday\": [{"
                + "\"date\": \"2025-07-10\","
                + "\"day\": {"
                + "\"condition\": {\"text\": \"Sunny\"},"
                + "\"avgtemp_c\": 30.0,"
                + "\"totalprecip_mm\": 0.0,"
                + "\"maxwind_kph\": 10.0"
                + "}}]}}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockWeatherResponse);

        // Mock ObjectMapper for good weather
        try {
            JsonNode mockRootNode = mock(JsonNode.class);
            JsonNode mockForecastNode = mock(JsonNode.class);
            JsonNode mockForecastDayNode = mock(JsonNode.class);
            JsonNode mockDayNode = mock(JsonNode.class);
            JsonNode mockConditionNode = mock(JsonNode.class);
            JsonNode mockDateNode = mock(JsonNode.class);
            JsonNode mockTempNode = mock(JsonNode.class);
            JsonNode mockPrecipNode = mock(JsonNode.class);
            JsonNode mockWindNode = mock(JsonNode.class);

            when(objectMapper.readTree(mockWeatherResponse)).thenReturn(mockRootNode);
            when(mockRootNode.path("forecast")).thenReturn(mockForecastNode);
            when(mockForecastNode.path("forecastday")).thenReturn(mockForecastDayNode);
            when(mockForecastDayNode.iterator()).thenReturn(Arrays.asList(mockDayNode).iterator());
            
            when(mockDayNode.get("date")).thenReturn(mockDateNode);
            when(mockDateNode.asText()).thenReturn("2025-07-10");
            
            when(mockDayNode.path("day")).thenReturn(mockDayNode);
            when(mockDayNode.path("condition")).thenReturn(mockConditionNode);
            when(mockConditionNode.path("text")).thenReturn(mockConditionNode);
            when(mockConditionNode.asText()).thenReturn("Sunny");
            
            when(mockDayNode.path("avgtemp_c")).thenReturn(mockTempNode);
            when(mockTempNode.asDouble()).thenReturn(30.0);
            
            when(mockDayNode.path("totalprecip_mm")).thenReturn(mockPrecipNode);
            when(mockPrecipNode.asDouble()).thenReturn(0.0);
            
            when(mockDayNode.path("maxwind_kph")).thenReturn(mockWindNode);
            when(mockWindNode.asDouble()).thenReturn(10.0);

        } catch (Exception e) {
            fail("Failed to mock ObjectMapper: " + e.getMessage());
        }

        // Act
        weatherMonitoringService.scheduleWeatherCheck();

        // Assert
        verify(tripPlanRepository).findUpcomingTripsInRange(any(LocalDate.class), any(LocalDate.class));
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    // Helper method to create isAdverseWeather method accessible for testing
    public boolean isAdverseWeather(Map<String, Object> weather) {
        List<String> alertReasons = new ArrayList<>();
        
        String condition = (String) weather.get("condition");
        Double precipMm = (Double) weather.get("precip_mm");
        Double windKph = (Double) weather.get("max_wind_kph");
        Double maxTemp = (Double) weather.get("avg_temp_c");

        if (condition != null) {
            String conditionLower = condition.toLowerCase();
            if (conditionLower.contains("storm") || conditionLower.contains("thunder") ||
                conditionLower.contains("heavy rain") || conditionLower.contains("torrential")) {
                alertReasons.add("Severe weather: " + condition);
            }
        }

        if (precipMm != null && precipMm > 15.0) {
            alertReasons.add("Heavy precipitation: " + precipMm + "mm");
        }

        if (windKph != null && windKph > 35.0) {
            alertReasons.add("Strong winds: " + windKph + " km/h");
        }

        if (maxTemp != null && (maxTemp > 35.0 || maxTemp < 10.0)) {
            alertReasons.add("Extreme temperature: " + maxTemp + "°C");
        }

        if (!alertReasons.isEmpty()) {
            weather.put("alert_reasons", alertReasons);
            return true;
        }

        return false;
    }
}

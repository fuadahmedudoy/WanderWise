package com.example.demo.service;

import com.example.demo.dto.TripPlanRequest;
import com.example.demo.service.CityDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TripPlannerService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CityDataService cityDataService;

    // Add these for database debugging
    @Autowired
    private javax.sql.DataSource dataSource;    // You can configure these in application.properties
    @Value("${python.travel.service.url:http://localhost:5001}")
    private String pythonServiceUrl;

    public Map<String, Object> planTrip(TripPlanRequest request, String userEmail) {
        try {            // 🔍 DEBUG: Log incoming request
            System.out.println("🔍 SPRING BOOT RECEIVED:");
            System.out.println("🗄️ ACTIVE DATABASE: " + System.getProperty("spring.profiles.active", "default"));
            
            // 🔍 DATABASE CONNECTION DEBUG
            try {
                System.out.println("🔗 DATABASE CONNECTION INFO:");
                System.out.println("   DataSource: " + dataSource.getClass().getSimpleName());
                java.sql.Connection conn = dataSource.getConnection();
                System.out.println("   Database URL: " + conn.getMetaData().getURL());
                System.out.println("   Database Product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("   Database Version: " + conn.getMetaData().getDatabaseProductVersion());
                conn.close();
            } catch (Exception e) {
                System.err.println("❌ Database connection error: " + e.getMessage());
            }
            
            System.out.println("Destination: " + request.getDestination());
            System.out.println("Start Date: " + request.getStartDate());
            System.out.println("Duration Days: " + request.getDurationDays());
            System.out.println("Budget: " + request.getBudget());
            System.out.println("Origin: " + request.getOrigin());
            System.out.println("User Email: " + userEmail);
            
            // 1. Fetch user-specific data from database if needed
            Map<String, Object> userData = getUserTravelData(userEmail);            // 2. Fetch city data from database
            System.out.println("🔄 Fetching city data for: " + request.getDestination());
            System.out.println("🔍 DEBUGGING DESTINATION FILTERING:");
            System.out.println("   Input destination: '" + request.getDestination() + "'");
            System.out.println("   Destination length: " + request.getDestination().length());
            System.out.println("   Destination toLowerCase: '" + request.getDestination().toLowerCase() + "'");
            
            Map<String, Object> cityData = cityDataService.getCityData(request.getDestination(), userEmail);
            System.out.println("🏛️ CITY DATA FETCHED:");
            System.out.println("City data available: " + (cityData != null));
            if (cityData != null) {
                System.out.println("City data keys: " + cityData.keySet());
                System.out.println("Success: " + cityData.getOrDefault("success", "unknown"));
                  // 🔍 DETAILED LOGGING OF FETCHED DATA
                if (cityData.get("city") != null) {
                    Map<?, ?> city = (Map<?, ?>) cityData.get("city");
                    System.out.println("📍 CITY INFO: " + city.get("name") + " (ID: " + city.get("id") + ")");
                    System.out.println("   Description: " + city.get("description"));
                    System.out.println("   🚨 CHECKING: Is this a generic fallback city? " + 
                        ("Beautiful destination in Bangladesh".equals(city.get("description"))));
                }
                
                if (cityData.get("spots") != null) {
                    java.util.List<?> spots = (java.util.List<?>) cityData.get("spots");
                    System.out.println("🏞️ SPOTS FETCHED: " + spots.size());
                    for (int i = 0; i < Math.min(spots.size(), 3); i++) {
                        Map<?, ?> spot = (Map<?, ?>) spots.get(i);
                        System.out.println("   [" + (i+1) + "] " + spot.get("name") + " - " + spot.get("description"));
                        if (spot.get("hotels") != null) {
                            System.out.println("       Hotels in this spot: " + ((java.util.List<?>) spot.get("hotels")).size());
                        }
                        if (spot.get("restaurants") != null) {
                            System.out.println("       Restaurants in this spot: " + ((java.util.List<?>) spot.get("restaurants")).size());
                        }
                    }
                    if (spots.size() > 3) {
                        System.out.println("   ... and " + (spots.size() - 3) + " more spots");
                    }
                } else {
                    System.out.println("🏞️ NO SPOTS FOUND for destination: " + request.getDestination());
                }
                
                // Count total hotels and restaurants across all spots
                int totalHotels = 0, totalRestaurants = 0;
                if (cityData.get("spots") != null) {
                    java.util.List<?> spots = (java.util.List<?>) cityData.get("spots");
                    for (Object spotObj : spots) {
                        Map<?, ?> spot = (Map<?, ?>) spotObj;
                        if (spot.get("hotels") != null) {
                            totalHotels += ((java.util.List<?>) spot.get("hotels")).size();
                        }
                        if (spot.get("restaurants") != null) {
                            totalRestaurants += ((java.util.List<?>) spot.get("restaurants")).size();
                        }
                    }
                }
                System.out.println("🏨 TOTAL HOTELS FETCHED: " + totalHotels);
                System.out.println("🍽️ TOTAL RESTAURANTS FETCHED: " + totalRestaurants);
                
                // Log data source
                System.out.println("📊 DATA SOURCE: " + cityData.getOrDefault("data_source", "unknown"));
                
            } else {
                System.out.println("❌ CITY DATA IS NULL!");
                cityData = new HashMap<>();
                cityData.put("success", false);
                cityData.put("error", "No city data available");
            }
            
            // 3. Prepare request for Python backend
            Map<String, Object> pythonRequest = preparePythonRequest(request, userData, cityData);
            
            // 4. Call Python travel service
            Map<String, Object> response = callPythonTravelService(pythonRequest);
            
            // 5. Process and return response
            return processResponse(response, userEmail);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to plan trip: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> getUserTravelData(String userEmail) {
        // Here you can fetch user preferences, past trips, etc. from your database
        Map<String, Object> userData = new HashMap<>();
        userData.put("userEmail", userEmail);
        // Add more user-specific data as needed
        return userData;
    }    private Map<String, Object> preparePythonRequest(TripPlanRequest request, Map<String, Object> userData, Map<String, Object> cityData) {
        Map<String, Object> pythonRequest = new HashMap<>();
        
        // Only send the 5 fields that frontend provides
        pythonRequest.put("destination", request.getDestination());
        pythonRequest.put("start_date", request.getStartDate());
        pythonRequest.put("duration_days", request.getDurationDays());
        pythonRequest.put("budget", request.getBudget());
        pythonRequest.put("origin", request.getOrigin());
        
        // Add backend-specific data
        pythonRequest.put("user_data", userData);
        pythonRequest.put("city_data", cityData);        // 🔍 DEBUG: Log what's being sent to Python
        System.out.println("🐍 SENDING TO PYTHON (5 core fields + city data):");
        System.out.println("destination: " + pythonRequest.get("destination"));
        System.out.println("start_date: " + pythonRequest.get("start_date"));
        System.out.println("duration_days: " + pythonRequest.get("duration_days"));
        System.out.println("budget: " + pythonRequest.get("budget"));
        System.out.println("origin: " + pythonRequest.get("origin"));
        System.out.println("city_data included: " + (pythonRequest.get("city_data") != null));
        System.out.println("user_data included: " + (pythonRequest.get("user_data") != null));
        
        // 🔍 DETAILED CITY DATA BEING SENT
        if (pythonRequest.get("city_data") != null) {
            Map<?, ?> cityDataToSend = (Map<?, ?>) pythonRequest.get("city_data");
            System.out.println("📤 CITY DATA TO PYTHON:");
            System.out.println("   Success: " + cityDataToSend.get("success"));
            if (cityDataToSend.get("spots") != null) {
                java.util.List<?> spots = (java.util.List<?>) cityDataToSend.get("spots");
                System.out.println("   Spots being sent: " + spots.size());
                
                // Count hotels and restaurants being sent
                int hotelsBeingSent = 0, restaurantsBeingSent = 0;
                for (Object spotObj : spots) {
                    Map<?, ?> spot = (Map<?, ?>) spotObj;
                    if (spot.get("hotels") != null) {
                        hotelsBeingSent += ((java.util.List<?>) spot.get("hotels")).size();
                    }
                    if (spot.get("restaurants") != null) {
                        restaurantsBeingSent += ((java.util.List<?>) spot.get("restaurants")).size();
                    }
                }
                System.out.println("   Hotels being sent: " + hotelsBeingSent);
                System.out.println("   Restaurants being sent: " + restaurantsBeingSent);
            }
        }
        
        return pythonRequest;
    }    private Map<String, Object> callPythonTravelService(Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            String url = pythonServiceUrl + "/plan-trip";
            System.out.println("🌐 CALLING PYTHON SERVICE: " + url);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            System.out.println("✅ PYTHON SERVICE RESPONSE:");
            System.out.println("   Status Code: " + response.getStatusCode());
            if (responseBody != null) {
                System.out.println("   Response Keys: " + responseBody.keySet());
                System.out.println("   Success: " + responseBody.getOrDefault("success", "unknown"));
                if (responseBody.get("trip_plan") != null) {
                    System.out.println("   Trip Plan Generated: YES");
                } else {
                    System.out.println("   Trip Plan Generated: NO");
                }
                if (responseBody.get("error") != null) {
                    System.out.println("   Error: " + responseBody.get("error"));
                }
            } else {
                System.out.println("   Response Body: NULL");
            }
            
            return responseBody;
            
        } catch (Exception e) {
            System.err.println("❌ PYTHON SERVICE ERROR: " + e.getMessage());
            throw new RuntimeException("Failed to communicate with Python service: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> processResponse(Map<String, Object> pythonResponse, String userEmail) {
        // Here you can:
        // 1. Save the trip plan to database
        // 2. Log the user activity
        // 3. Process/format the response as needed
        
        Map<String, Object> finalResponse = new HashMap<>(pythonResponse);
        finalResponse.put("processed_by", "spring-boot-gateway");
        finalResponse.put("user_email", userEmail);
        
        return finalResponse;    }
}

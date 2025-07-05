package com.example.demo.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class WeatherTestController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Replace with your actual WeatherAPI key
    private static final String API_KEY = "c0859dd16f5c4108a0571407250307";

    @PostMapping("/weather")
    public Map<String, Object> getWeather(@RequestBody Map<String, Object> request) {
        try {
            String location = (String) request.get("location");
            int days = ((Number) request.getOrDefault("days", 3)).intValue();

            System.out.println("🌤️ Fetching WeatherAPI forecast for: " + location);

            Map<String, Object> weatherData = fetchWeatherAPIData(location, days);

            return Map.of(
                    "success", true,
                    "location", location,
                    "weather", weatherData
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Failed to fetch weather: " + e.getMessage());
        }
    }

    @GetMapping("/weather")
    public Map<String, Object> getWeatherGet(
            @RequestParam String location,
            @RequestParam(defaultValue = "3") int days) {

        Map<String, Object> request = new HashMap<>();
        request.put("location", location);
        request.put("days", days);
        return getWeather(request);
    }

    // TEST WEATHER MONITORING LOGIC
    @PostMapping("/test-monitoring")
    public Map<String, Object> testWeatherMonitoring() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🧪 TESTING WEATHER MONITORING - " + timestamp);
        System.out.println("=".repeat(80));

        try {
            // Simulate upcoming trips (like database data)
            List<Map<String, Object>> mockTrips = Arrays.asList(
                Map.of(
                    "id", "trip-001",
                    "destination", "Dhaka",
                    "origin", "Chittagong", 
                    "startDate", LocalDate.now().plusDays(2).toString(),
                    "duration", 3,
                    "userEmail", "john@example.com",
                    "budget", 25000.0
                ),
                Map.of(
                    "id", "trip-002", 
                    "destination", "Cox's Bazar",
                    "origin", "Dhaka",
                    "startDate", LocalDate.now().plusDays(1).toString(),
                    "duration", 4,
                    "userEmail", "jane@example.com",
                    "budget", 35000.0
                ),
                Map.of(
                    "id", "trip-003",
                    "destination", "Rangpur", 
                    "origin", "Dhaka",
                    "startDate", LocalDate.now().plusDays(5).toString(),
                    "duration", 2,
                    "userEmail", "mike@example.com",
                    "budget", 20000.0
                )
            );

            System.out.println("📊 Simulating " + mockTrips.size() + " upcoming trips from database");
            
            List<Map<String, Object>> alertResults = new ArrayList<>();
            
            for (Map<String, Object> trip : mockTrips) {
                Map<String, Object> result = checkTripWeather(trip);
                alertResults.add(result);
            }

            System.out.println("✅ Weather monitoring test completed at " + timestamp);
            System.out.println("=".repeat(80) + "\n");

            return Map.of(
                "success", true,
                "timestamp", timestamp,
                "tripsChecked", mockTrips.size(),
                "results", alertResults,
                "message", "Check console for detailed weather monitoring output"
            );

        } catch (Exception e) {
            System.err.println("❌ Error during weather monitoring test: " + e.getMessage());
            e.printStackTrace();
            return Map.of("error", "Test failed: " + e.getMessage());
        }
    }

    private Map<String, Object> checkTripWeather(Map<String, Object> trip) {
        try {
            String destination = (String) trip.get("destination");
            String userEmail = (String) trip.get("userEmail");
            Integer duration = (Integer) trip.get("duration");
            String startDate = (String) trip.get("startDate");

            System.out.println("\n🔍 CHECKING TRIP WEATHER:");
            System.out.println("   🆔 Trip ID: " + trip.get("id"));
            System.out.println("   📍 Destination: " + destination);
            System.out.println("   🏠 Origin: " + trip.get("origin"));
            System.out.println("   📅 Start Date: " + startDate);
            System.out.println("   ⏱️ Duration: " + duration + " days");
            System.out.println("   👤 User: " + userEmail);
            System.out.println("   💰 Budget: ৳" + trip.get("budget"));

            Map<String, Object> weatherData = fetchWeatherAPIData(destination, duration);

            if (weatherData != null && weatherData.containsKey("forecast")) {
                List<Map<String, Object>> forecast = (List<Map<String, Object>>) weatherData.get("forecast");
                
                boolean foundAdverseWeather = false;
                List<String> alerts = new ArrayList<>();
                
                System.out.println("🌤️ Weather forecast for " + forecast.size() + " days:");
                
                for (Map<String, Object> dayWeather : forecast) {
                    String date = (String) dayWeather.get("date");
                    String condition = (String) dayWeather.get("condition");
                    Double temp = (Double) dayWeather.get("avg_temp_c");
                    
                    System.out.println("   📅 " + date + ": " + condition + " (" + temp + "°C)");
                    
                    if (isAdverseWeatherCondition(dayWeather)) {
                        if (!foundAdverseWeather) {
                            System.out.println("\n🚨 WEATHER ALERT FOR " + destination.toUpperCase() + " 🚨");
                            foundAdverseWeather = true;
                        }
                        
                        String alertMsg = printWeatherAlert(dayWeather);
                        alerts.add(alertMsg);
                    }
                }
                
                if (!foundAdverseWeather) {
                    System.out.println("✅ Weather conditions look good for this trip!");
                }
                
                System.out.println("-".repeat(60));

                return Map.of(
                    "tripId", trip.get("id"),
                    "destination", destination,
                    "hasAlerts", foundAdverseWeather,
                    "alerts", alerts,
                    "forecastDays", forecast.size()
                );

            } else {
                System.out.println("❌ Failed to retrieve weather data for " + destination);
                return Map.of(
                    "tripId", trip.get("id"),
                    "destination", destination,
                    "error", "Failed to get weather data"
                );
            }

        } catch (Exception e) {
            System.err.println("❌ Error checking weather for trip: " + e.getMessage());
            return Map.of(
                "tripId", trip.get("id"),
                "error", e.getMessage()
            );
        }
    }

    private boolean isAdverseWeatherCondition(Map<String, Object> weather) {
        List<String> alertReasons = new ArrayList<>();
        
        String condition = (String) weather.get("condition");
        Double precipMm = (Double) weather.get("precip_mm");
        Double windKph = (Double) weather.get("max_wind_kph");
        Double maxTemp = (Double) weather.get("avg_temp_c");

        // Check severe weather conditions
        if (condition != null) {
            String conditionLower = condition.toLowerCase();
            if (conditionLower.contains("storm") || conditionLower.contains("thunder") ||
                conditionLower.contains("heavy rain") || conditionLower.contains("torrential")) {
                alertReasons.add("Severe weather: " + condition);
            }
        }

        // High precipitation threshold
        if (precipMm != null && precipMm > 15.0) {
            alertReasons.add("Heavy precipitation: " + precipMm + "mm");
        }

        // Strong wind threshold  
        if (windKph != null && windKph > 35.0) {
            alertReasons.add("Strong winds: " + windKph + " km/h");
        }

        // Temperature extremes
        if (maxTemp != null && (maxTemp > 35.0 || maxTemp < 10.0)) {
            alertReasons.add("Extreme temperature: " + maxTemp + "°C");
        }

        if (!alertReasons.isEmpty()) {
            weather.put("alert_reasons", alertReasons);
            return true;
        }

        return false;
    }

    private String printWeatherAlert(Map<String, Object> weather) {
        String date = (String) weather.get("date");
        String condition = (String) weather.get("condition");
        Double temp = (Double) weather.get("avg_temp_c");
        Double precipMm = (Double) weather.get("precip_mm");
        Double windKph = (Double) weather.get("max_wind_kph");
        List<String> alertReasons = (List<String>) weather.get("alert_reasons");

        String alertMsg = String.format("⚠️ %s: %s (%.1f°C, %.1fmm rain, %.1f km/h wind)", 
                                       date, condition, temp, precipMm, windKph);

        System.out.println("   📅 Alert Date: " + date);
        System.out.println("   🌦️ Condition: " + condition); 
        System.out.println("   🌡️ Temperature: " + temp + "°C");
        System.out.println("   🌧️ Precipitation: " + precipMm + "mm");
        System.out.println("   💨 Wind Speed: " + windKph + " km/h");
        System.out.println("   ⚠️ Reasons: " + String.join(", ", alertReasons));
        System.out.println("   📋 Recommendation: Pack weather gear, consider indoor activities");

        return alertMsg;
    }

    private Map<String, Object> fetchWeatherAPIData(String location, int days) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("http://api.weatherapi.com/v1/forecast.json")
                    .queryParam("key", API_KEY)
                    .queryParam("q", location)
                    .queryParam("days", days)
                    .queryParam("aqi", "no")
                    .queryParam("alerts", "no")
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode forecastDays = root.path("forecast").path("forecastday");

            List<Map<String, Object>> forecastList = new ArrayList<>();

            for (JsonNode day : forecastDays) {
                Map<String, Object> dayInfo = new HashMap<>();
                dayInfo.put("date", day.get("date").asText());
                dayInfo.put("condition", day.path("day").path("condition").path("text").asText());
                dayInfo.put("icon", "https:" + day.path("day").path("condition").path("icon").asText());
                dayInfo.put("avg_temp_c", day.path("day").path("avgtemp_c").asDouble());
                dayInfo.put("humidity", day.path("day").path("avghumidity").asDouble());
                dayInfo.put("precip_mm", day.path("day").path("totalprecip_mm").asDouble());
                dayInfo.put("max_wind_kph", day.path("day").path("maxwind_kph").asDouble());

                forecastList.add(dayInfo);
            }

            return Map.of("forecast", forecastList);

        } catch (Exception e) {
            throw new RuntimeException("WeatherAPI error: " + e.getMessage());
        }
    }
}

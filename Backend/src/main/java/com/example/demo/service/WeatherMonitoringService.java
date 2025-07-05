package com.example.demo.service;

import com.example.demo.Repository.TripPlanRepository;
import com.example.demo.entity.TripPlan;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WeatherMonitoringService {

    @Autowired
    private TripPlanRepository tripPlanRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy // Add Lazy to avoid circular dependency
    private NotificationService notificationService;

    private static final String API_KEY = "c0859dd16f5c4108a0571407250307";

    // Run every 10 minutes for testing (change to longer for production)
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scheduleWeatherCheck() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        System.out.println("\n" + "=".repeat(90));
        System.out.println("🌤️ AUTOMATED WEATHER MONITORING - " + timestamp);
        System.out.println("=".repeat(90));
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate checkUntil = today.plusDays(7); // Check next 7 days
            
            // Query real trip_plan table
            List<TripPlan> upcomingTrips = tripPlanRepository.findUpcomingTripsInRange(today, checkUntil);
            
            System.out.println("📊 Database Query Results:");
            System.out.println("   🔍 Checking trips between " + today + " and " + checkUntil);
            System.out.println("   📅 Found " + upcomingTrips.size() + " upcoming trips in database");
            
            if (upcomingTrips.isEmpty()) {
                System.out.println("✅ No upcoming trips found for weather monitoring");
                
                // Show available trips for debugging
                List<TripPlan> allUpcoming = tripPlanRepository.findByStatus(TripPlan.TripStatus.UPCOMING);
                System.out.println("📋 Total upcoming trips in database: " + allUpcoming.size());
                
                if (!allUpcoming.isEmpty()) {
                    System.out.println("📝 Available upcoming trips:");
                    for (TripPlan trip : allUpcoming.subList(0, Math.min(3, allUpcoming.size()))) {
                        System.out.println("   • ID: " + trip.getId() + 
                                         ", Destination: " + trip.getDestination() + 
                                         ", Start: " + trip.getStartDate());
                    }
                }
            } else {
                System.out.println("🎯 Processing upcoming trips for weather alerts:");
                
                for (TripPlan trip : upcomingTrips) {
                    checkTripWeatherFromDB(trip);
                }
            }
            
            System.out.println("✅ Automated weather check completed at " + timestamp);
            System.out.println("=".repeat(90) + "\n");
            
        } catch (Exception e) {
            System.err.println("❌ Error during automated weather monitoring: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkTripWeatherFromDB(TripPlan tripPlan) {
        try {
            String destination = tripPlan.getDestination();
            LocalDate startDate = tripPlan.getStartDate();
            Integer duration = tripPlan.getDurationDays();
            
            // Safe user access - avoid lazy loading issues
            String userInfo;
            try {
                userInfo = tripPlan.getUser() != null ? tripPlan.getUser().getEmail() : "User ID: " + tripPlan.getUserId();
            } catch (Exception e) {
                userInfo = "User ID: " + tripPlan.getUserId(); // Fallback if user loading fails
            }
            
            System.out.println("\n🔍 CHECKING DATABASE TRIP:");
            System.out.println("   🆔 Trip ID: " + tripPlan.getId());
            System.out.println("   📍 Destination: " + destination);
            System.out.println("   🏠 Origin: " + tripPlan.getOrigin());
            System.out.println("   📅 Start Date: " + startDate);
            System.out.println("   ⏱️ Duration: " + duration + " days");
            System.out.println("   👤 User: " + userInfo);
            System.out.println("   💰 Budget: ৳" + tripPlan.getBudget());
            System.out.println("   📋 Status: " + tripPlan.getStatus());
            
            if (destination == null || startDate == null || duration == null) {
                System.out.println("⚠️ Missing essential trip data - skipping weather check");
                return;
            }
            
            Map<String, Object> weatherData = fetchWeatherData(destination, duration);
            
            if (weatherData != null && weatherData.containsKey("forecast")) {
                List<Map<String, Object>> forecast = (List<Map<String, Object>>) weatherData.get("forecast");
                
                boolean foundAdverseWeather = true;//for testing actually false

                StringBuilder weatherAlertInfo = new StringBuilder();
                
                System.out.println("🌤️ Weather forecast for " + forecast.size() + " days:");
                
                for (Map<String, Object> dayWeather : forecast) {
                    String date = (String) dayWeather.get("date");
                    String condition = (String) dayWeather.get("condition");
                    Double temp = (Double) dayWeather.get("avg_temp_c");
                    
                    System.out.println("   📅 " + date + ": " + condition + " (" + temp + "°C)");
                    
                    if (isAdverseWeather(dayWeather)) {
                        if (!foundAdverseWeather) {
                            System.out.println("\n🚨 WEATHER ALERT FOR " + destination.toUpperCase() + " 🚨");
                            System.out.println("👤 User: " + userInfo);
                            foundAdverseWeather = true;
                        }
                        
                        printWeatherAlert(dayWeather);
                        
                        // Build alert info for notification
                        List<String> alertReasons = (List<String>) dayWeather.get("alert_reasons");
                        weatherAlertInfo.append(date).append(": ").append(String.join(", ", alertReasons)).append(". ");
                    }
                }
                
                // Send notification if adverse weather found
                if (foundAdverseWeather && tripPlan.getUserId() != null) {
                    String alertMessage = "Weather alert detected for your trip to " + destination + ". " + 
                                        weatherAlertInfo.toString() + 
                                        "Please pack appropriate gear and monitor weather updates.";
                    System.out.println(tripPlan.getUserId());
                    notificationService.sendWeatherAlert(
                        tripPlan.getUserId(), 
                        destination, 
                        alertMessage, 
                        tripPlan.getId()
                    );
                    System.out.println("*** notification sent****");
                    
                    System.out.println("📱 Notification sent to user: " + tripPlan.getUserId());
                }
                
                if (!foundAdverseWeather) {
                    System.out.println("✅ Weather conditions look favorable for this trip!");
                }
            } else {
                System.out.println("❌ Failed to retrieve weather data for " + destination);
            }
            
            System.out.println("-".repeat(70));
            
        } catch (Exception e) {
            System.err.println("❌ Error checking weather for trip " + tripPlan.getId() + ": " + e.getMessage());
            e.printStackTrace(); // Add full stack trace for debugging
        }
    }

    // Manual trigger for testing
    @Transactional(readOnly = true)
    public void manualWeatherCheck() {
        System.out.println("🔧 MANUAL WEATHER CHECK TRIGGERED");
        scheduleWeatherCheck();
    }

    // Copy the working methods from WeatherTestController
    private Map<String, Object> fetchWeatherData(String location, int days) {
        try {
            String url = UriComponentsBuilder
                .fromHttpUrl("http://api.weatherapi.com/v1/forecast.json")
                .queryParam("key", API_KEY)
                .queryParam("q", location)
                .queryParam("days", Math.min(days, 3))
                .queryParam("aqi", "no")
                .queryParam("alerts", "yes")
                .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode forecastDays = root.path("forecast").path("forecastday");

            List<Map<String, Object>> forecastList = new ArrayList<>();

            for (JsonNode day : forecastDays) {
                Map<String, Object> dayInfo = new HashMap<>();
                dayInfo.put("date", day.get("date").asText());
                dayInfo.put("condition", day.path("day").path("condition").path("text").asText());
                dayInfo.put("avg_temp_c", day.path("day").path("avgtemp_c").asDouble());
                dayInfo.put("precip_mm", day.path("day").path("totalprecip_mm").asDouble());
                dayInfo.put("max_wind_kph", day.path("day").path("maxwind_kph").asDouble());

                forecastList.add(dayInfo);
            }

            return Map.of("forecast", forecastList);

        } catch (Exception e) {
            System.err.println("❌ Weather API error for " + location + ": " + e.getMessage());
            return null;
        }
    }

    private boolean isAdverseWeather(Map<String, Object> weather) {
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

    private void printWeatherAlert(Map<String, Object> weather) {
        String date = (String) weather.get("date");
        String condition = (String) weather.get("condition");
        Double temp = (Double) weather.get("avg_temp_c");
        Double precipMm = (Double) weather.get("precip_mm");
        Double windKph = (Double) weather.get("max_wind_kph");
        List<String> alertReasons = (List<String>) weather.get("alert_reasons");

        System.out.println("   📅 Alert Date: " + date);
        System.out.println("   🌦️ Condition: " + condition);
        System.out.println("   🌡️ Temperature: " + temp + "°C");
        System.out.println("   🌧️ Precipitation: " + precipMm + "mm");
        System.out.println("   💨 Wind Speed: " + windKph + " km/h");
        System.out.println("   ⚠️ Alert Reasons: " + String.join(", ", alertReasons));
        System.out.println("   📋 Recommendation: Pack appropriate weather gear, monitor updates");
    }

    public Map<String, Object> getWeatherDetailsForTrip(Long tripId) {
        TripPlan trip = tripPlanRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Map<String, Object> weatherData = fetchWeatherData(trip.getDestination(), trip.getDurationDays());
        
        if (weatherData != null && weatherData.containsKey("forecast")) {
            List<Map<String, Object>> forecast = (List<Map<String, Object>>) weatherData.get("forecast");
            
            // Process and format the weather data
            Map<String, Object> processedData = new HashMap<>();
            processedData.put("location", trip.getDestination());
            processedData.put("tripStartDate", trip.getStartDate());
            processedData.put("forecast", forecast);
            
            // Add any weather alerts
            List<String> alerts = new ArrayList<>();
            for (Map<String, Object> dayWeather : forecast) {
                if (isAdverseWeather(dayWeather)) {
                    List<String> alertReasons = (List<String>) dayWeather.get("alert_reasons");
                    alerts.addAll(alertReasons);
                }
            }
            processedData.put("alerts", alerts);
            
            return processedData;
        }
        
        throw new RuntimeException("Failed to fetch weather data");
    }
}
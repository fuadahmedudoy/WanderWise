package com.example.demo.Controller;

import com.example.demo.service.WeatherMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherMonitoringService weatherMonitoringService;

    @GetMapping("/details/{tripId}")
    public ResponseEntity<?> getWeatherDetails(@PathVariable Long tripId) {
        try {
            Map<String, Object> weatherDetails = weatherMonitoringService.getWeatherDetailsForTrip(tripId);
            return ResponseEntity.ok(weatherDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching weather details: " + e.getMessage());
        }
    }
} 
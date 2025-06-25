package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TripPlanRequest {
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @NotBlank(message = "Start date is required")
    private String startDate;
    
    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer durationDays;
    
    private Double budget;
    
    private String origin;
    
    // Additional fields that might be useful
    private String preferences;
    private String travelStyle;
    private Integer numberOfTravelers;
}

package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptTripRequest {
    private Map<String, Object> tripPlan;
    private String tripName;
    private String notes;
}

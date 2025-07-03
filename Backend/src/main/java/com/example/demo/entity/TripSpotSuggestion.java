package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trip_spot_suggestions")
public class TripSpotSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "trip_plan_id", nullable = false)
    private UUID tripPlanId;

    @Column(nullable = false)
    private String name;

    // @Column(columnDefinition = "TEXT")
    // private String description;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "recommended_time", length = 100)
    private String recommendedTime;

    @Column(name = "estimated_duration_hours", precision = 4, scale = 1)
    private BigDecimal estimatedDurationHours;
}

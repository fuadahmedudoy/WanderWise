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
@Table(name = "trip_food_options")
public class TripFoodOption {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "trip_plan_id", nullable = false)
    private UUID tripPlanId;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "meal_type", nullable = false, length = 50)
    private String mealType;

    @Column(nullable = false)
    private String title;

    // @Column(columnDefinition = "TEXT")
    // private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @Column(length = 100)
    private String category;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    // @Column(columnDefinition = "TEXT")
    // private String website;

    @Column(length = 20)
    private String cost;
}

package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "featured_destinations")
public class FeaturedDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String title;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer days;

    @Column(name = "avg_rating", nullable = false)
    private Double avgRating;

    @Column
    @ElementCollection(targetClass = String.class)
    private String[] reviews;

    @Column(name = "is_active")
    private boolean isActive;
}
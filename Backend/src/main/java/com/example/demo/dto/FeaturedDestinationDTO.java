package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedDestinationDTO {
    private UUID id;
    private String destination;
    private String title;
    private String imageUrl;
    private String description;
    private Integer days;
    private Double avgRating;
    private String[] reviews;
}
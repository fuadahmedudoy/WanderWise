package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeaturedDestinationRequest {
    @NotBlank(message = "Destination name is required")
    private String destination;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Number of days is required")
    @Positive(message = "Days must be a positive number")
    private Integer days;
    
    @NotNull(message = "Average rating is required")
    @Positive(message = "Average rating must be a positive number")
    private Double avgRating;
    
    private Boolean isActive;
    
    // Image file to be uploaded
    private MultipartFile image;
}

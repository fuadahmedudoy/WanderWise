// Code/Backend/src/main/java/com/example/demo/dto/CreateBlogPostRequest.java
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogPostRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String[] tags; // Optional, can be null or empty

    private Boolean isPublic = true; // Defaults to true if not provided
}
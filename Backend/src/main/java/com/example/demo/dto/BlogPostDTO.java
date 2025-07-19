package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostDTO {
    private UUID id;
    private UUID userId;
    private String title;
    private String content;
    private String imageUrl;
    private String[] tags;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Simplified user info
    private String username;
    private String userEmail;
    
    // Interaction counts
    private Long likeCount;
    private Long commentCount;
    private Boolean isLikedByCurrentUser; // null if user not authenticated
}
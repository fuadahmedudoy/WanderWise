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
public class BlogLikeDTO {
    private UUID id;
    private UUID blogPostId;
    private UUID userId;
    private String username;
    private String userEmail;
    private LocalDateTime createdAt;
}
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCommentDTO {
    private UUID id;
    private UUID blogPostId;
    private UUID userId;
    private String content;
    private UUID parentCommentId;
    private String username;
    private String userEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BlogCommentDTO> replies;
}
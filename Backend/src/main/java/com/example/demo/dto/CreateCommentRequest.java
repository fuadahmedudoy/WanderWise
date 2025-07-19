package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    @NotBlank(message = "Comment content cannot be empty")
    private String content;
    
    private UUID parentCommentId; // Optional, for replies
}
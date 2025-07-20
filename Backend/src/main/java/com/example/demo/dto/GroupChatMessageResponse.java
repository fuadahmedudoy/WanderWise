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
public class GroupChatMessageResponse {
    private UUID id;
    private UUID groupTripId;
    private UUID senderId;
    private String senderName;
    private String message;
    private LocalDateTime timestamp;
    private boolean isCurrentUser;
}

package com.example.demo.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "group_trip_id", nullable = false)
    private UUID groupTripId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

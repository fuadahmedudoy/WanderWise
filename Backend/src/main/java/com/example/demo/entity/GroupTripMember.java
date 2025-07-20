package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "group_trip_members")
public class GroupTripMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "group_trip_id", nullable = false)
    private UUID groupTripId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "join_message", columnDefinition = "TEXT")
    private String joinMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_trip_id", insertable = false, updatable = false)
    private GroupTrip groupTrip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = MemberStatus.INVITED;
        }
    }

    public enum MemberStatus {
        INVITED,    // User was invited to join
        ACCEPTED,   // User accepted the invitation/request
        REQUESTED,  // User requested to join
        DECLINED    // User declined the invitation or was rejected
    }
}

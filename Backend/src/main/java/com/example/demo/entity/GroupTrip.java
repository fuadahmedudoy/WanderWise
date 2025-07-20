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
@Table(name = "group_trips")
public class GroupTrip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "description")
    private String description;

    @Column(name = "max_people")
    private Integer maxPeople;

    @Column(name = "meeting_point", length = 500)
    private String meetingPoint;

    @Column(name = "additional_requirements", columnDefinition = "TEXT")
    private String additionalRequirements;

    @Column(name = "current_members")
    private Integer currentMembers;

    @Column(name = "trip_plan_id", nullable = false)
    private Long tripPlanId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupTripStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private User creator;

    // For the trip plan, we'll need to join with the trip_plans table or accepted_trips table
    @Transient
    private String tripPlan; // This will be populated manually in the service

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = GroupTripStatus.OPEN;
        }
        if (currentMembers == null) {
            currentMembers = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum GroupTripStatus {
        OPEN,        // Group is open and accepting members
        FULL,        // Group has reached maximum capacity
        CLOSED,      // Group is no longer accepting members
        COMPLETED    // Trip has been completed
    }
}

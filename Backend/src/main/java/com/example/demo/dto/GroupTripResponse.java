package com.example.demo.dto;

import com.example.demo.entity.GroupTrip;
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
public class GroupTripResponse {
    private UUID id;
    private String groupName;
    private String description;
    private Integer maxPeople;
    private String meetingPoint;
    private String additionalRequirements;
    private UUID createdByUserId;
    private String creatorName;
    private Long tripPlanId;
    private Object tripPlan;
    private GroupTrip.GroupTripStatus status;
    private Integer currentMembers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isCreator;
    private Boolean hasRequested;
    private String memberStatus;
    private String userJoinStatus;
    private List<GroupTripMemberResponse> members;
}

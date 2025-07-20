package com.example.demo.dto;

import com.example.demo.entity.GroupTripMember;
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
public class GroupTripMemberResponse {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private GroupTripMember.MemberStatus status;
    private LocalDateTime joinedAt;
    private String joinMessage;
}

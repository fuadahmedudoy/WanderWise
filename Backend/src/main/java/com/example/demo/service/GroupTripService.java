package com.example.demo.service;

import com.example.demo.Repository.TripPlanRepository;
import com.example.demo.Repository.GroupTripMemberRepository;
import com.example.demo.Repository.GroupTripRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.GroupChatMessageRepository;
import com.example.demo.dto.*;
import com.example.demo.entity.TripPlan;
import com.example.demo.entity.GroupTrip;
import com.example.demo.entity.GroupTripMember;
import com.example.demo.entity.User;
import com.example.demo.entity.GroupChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupTripService {

    private final GroupTripRepository groupTripRepository;
    private final GroupTripMemberRepository groupTripMemberRepository;
    private final UserRepository userRepository;
    private final TripPlanRepository tripPlanRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApiResponse<GroupTripResponse> createGroupTrip(CreateGroupTripRequest request, UUID creatorId) {
        try {
            System.out.println("=== DEBUG: Starting group trip creation ===");
            System.out.println("Creator ID: " + creatorId);
            System.out.println("Request: " + request);

            // Step 1: Validate user exists
            System.out.println("Step 1: Checking if user exists...");
            User creator = userRepository.findById(creatorId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + creatorId));
            System.out.println("User found: " + creator.getUsername());

            // Step 2: Basic validation
            System.out.println("Step 2: Validating request data...");
            if (request.getGroupName() == null || request.getGroupName().trim().isEmpty()) {
                System.out.println("ERROR: Group name is missing or empty");
                return ApiResponse.<GroupTripResponse>builder()
                        .success(false)
                        .error("Group name is required")
                        .build();
            }

            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                System.out.println("ERROR: Description is missing or empty");
                return ApiResponse.<GroupTripResponse>builder()
                        .success(false)
                        .error("Description is required")
                        .build();
            }

            // Step 3: Find user's trips
            System.out.println("Step 3: Finding user's trip plans...");
            List<TripPlan> userTrips = tripPlanRepository.findByUserIdOrderByCreatedAtDesc(creatorId);
            System.out.println("Found " + userTrips.size() + " trip plans");
            
            if (userTrips.isEmpty()) {
                System.out.println("ERROR: No trip plans found");
                // Let's also check if there are any trips for any user to see if the table has data
                long totalTrips = tripPlanRepository.count();
                System.out.println("DEBUG: Total trip plans in database: " + totalTrips);
                
                return ApiResponse.<GroupTripResponse>builder()
                        .success(false)
                        .error("No trip plans found. Please accept a trip first before creating a group trip.")
                        .build();
            }

            // Step 4: Create group trip entity
            System.out.println("Step 4: Creating group trip entity...");
            TripPlan latestTrip = userTrips.get(0);
            Long tripPlanId = latestTrip.getId();
            
            GroupTrip groupTrip = GroupTrip.builder()
                    .groupName(request.getGroupName().trim())
                    .description(request.getDescription().trim())
                    .maxPeople(request.getMaxPeople() != null ? request.getMaxPeople() : 5)
                    .meetingPoint(request.getMeetingPoint())
                    .additionalRequirements(request.getAdditionalRequirements())
                    .tripPlanId(tripPlanId)
                    .createdByUserId(creatorId)
                    .currentMembers(1)
                    .status(GroupTrip.GroupTripStatus.OPEN)
                    .build();

            System.out.println("Group trip entity created: " + groupTrip.getGroupName());

            // Step 5: Save to database
            System.out.println("Step 5: Saving to database...");
            System.out.println("GroupTrip before save: " + groupTrip);
            System.out.println("Repository: " + groupTripRepository.getClass().getName());
            
            GroupTrip savedGroupTrip;
            try {
                savedGroupTrip = groupTripRepository.save(groupTrip);
                System.out.println("Saved successfully with ID: " + savedGroupTrip.getId());
                System.out.println("Saved GroupTrip: " + savedGroupTrip);
                
                // Verify it's actually in the database
                long count = groupTripRepository.count();
                System.out.println("Total group trips in database: " + count);
                
            } catch (Exception saveException) {
                System.out.println("ERROR during save: " + saveException.getMessage());
                saveException.printStackTrace();
                throw saveException;
            }

            // Step 6: Create response
            System.out.println("Step 6: Creating response...");
            savedGroupTrip.setTripPlan(latestTrip.getTripPlan());

            GroupTripResponse response = GroupTripResponse.builder()
                    .id(savedGroupTrip.getId())
                    .groupName(savedGroupTrip.getGroupName())
                    .description(savedGroupTrip.getDescription())
                    .maxPeople(savedGroupTrip.getMaxPeople())
                    .meetingPoint(savedGroupTrip.getMeetingPoint())
                    .additionalRequirements(savedGroupTrip.getAdditionalRequirements())
                    .createdByUserId(savedGroupTrip.getCreatedByUserId())
                    .creatorName(creator.getUsername())
                    .tripPlanId(savedGroupTrip.getTripPlanId())
                    .tripPlan(latestTrip.getTripPlan())
                    .status(savedGroupTrip.getStatus())
                    .currentMembers(1)
                    .createdAt(savedGroupTrip.getCreatedAt())
                    .updatedAt(savedGroupTrip.getUpdatedAt())
                    .isCreator(true)
                    .hasRequested(false)
                    .memberStatus("CREATOR")
                    .build();

            System.out.println("=== DEBUG: Group trip creation successful ===");
            return ApiResponse.<GroupTripResponse>builder()
                    .success(true)
                    .data(response)
                    .message("Group trip created successfully")
                    .build();

        } catch (Exception e) {
            System.err.println("=== ERROR: Group trip creation failed ===");
            e.printStackTrace();
            return ApiResponse.<GroupTripResponse>builder()
                    .success(false)
                    .error("Failed to create group trip: " + e.getMessage())
                    .build();
        }
    }

    public ApiResponse<List<GroupTripResponse>> getAllAvailableGroupTrips(UUID userId) {
        try {
            List<GroupTrip> groupTrips = groupTripRepository.findAvailableGroupTripsForUser(userId);
            
            List<GroupTripResponse> responses = groupTrips.stream()
                    .map(trip -> convertToResponse(trip, userId))
                    .collect(Collectors.toList());

            return ApiResponse.<List<GroupTripResponse>>builder()
                    .success(true)
                    .data(responses)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<List<GroupTripResponse>>builder()
                    .success(false)
                    .error("Failed to fetch group trips: " + e.getMessage())
                    .build();
        }
    }

    public ApiResponse<List<GroupTripResponse>> getMyGroupTrips(UUID userId) {
        try {
            // Get trips created by user
            List<GroupTrip> createdTrips = groupTripRepository.findByCreatedByUserIdOrderByCreatedAtDesc(userId);
            
            // Get trips where user is a member (joined or requested)
            List<GroupTripMember> memberEntries = groupTripMemberRepository.findByUserIdOrderByJoinedAtDesc(userId);
            List<GroupTrip> memberTrips = memberEntries.stream()
                    .map(member -> groupTripRepository.findById(member.getGroupTripId()).orElse(null))
                    .filter(trip -> trip != null)
                    .collect(Collectors.toList());
            
            // Combine both lists and remove duplicates
            List<GroupTrip> allTrips = new java.util.ArrayList<>(createdTrips);
            for (GroupTrip memberTrip : memberTrips) {
                if (!allTrips.stream().anyMatch(trip -> trip.getId().equals(memberTrip.getId()))) {
                    allTrips.add(memberTrip);
                }
            }
            
            // Sort by creation date (most recent first)
            allTrips.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            
            List<GroupTripResponse> responses = allTrips.stream()
                    .map(trip -> convertToResponse(trip, userId))
                    .collect(Collectors.toList());

            return ApiResponse.<List<GroupTripResponse>>builder()
                    .success(true)
                    .data(responses)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<List<GroupTripResponse>>builder()
                    .success(false)
                    .error("Failed to fetch your group trips: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponse<String> joinGroupTrip(UUID groupTripId, JoinGroupTripRequest request, UUID userId) {
        try {
            // Check if group trip exists
            GroupTrip groupTrip = groupTripRepository.findById(groupTripId)
                    .orElseThrow(() -> new RuntimeException("Group trip not found"));

            // Check if user is the creator
            if (groupTrip.getCreatedByUserId().equals(userId)) {
                return ApiResponse.<String>builder()
                        .success(false)
                        .error("You cannot join your own group trip")
                        .build();
            }

            // Check if user already has an active request (REQUESTED or ACCEPTED)
            Optional<GroupTripMember> existingMember = groupTripMemberRepository
                    .findByGroupTripIdAndUserId(groupTripId, userId);
            
            if (existingMember.isPresent()) {
                GroupTripMember.MemberStatus status = existingMember.get().getStatus();
                if (status == GroupTripMember.MemberStatus.REQUESTED) {
                    return ApiResponse.<String>builder()
                            .success(false)
                            .error("You have already requested to join this group trip")
                            .build();
                } else if (status == GroupTripMember.MemberStatus.ACCEPTED) {
                    return ApiResponse.<String>builder()
                            .success(false)
                            .error("You are already a member of this group trip")
                            .build();
                }
                // If status is DECLINED, allow them to request again by updating the existing record
                if (status == GroupTripMember.MemberStatus.DECLINED) {
                    existingMember.get().setStatus(GroupTripMember.MemberStatus.REQUESTED);
                    existingMember.get().setJoinMessage(request.getJoinMessage());
                    existingMember.get().setJoinedAt(LocalDateTime.now()); // Update to current time for new request
                    groupTripMemberRepository.save(existingMember.get());
                    
                    return ApiResponse.<String>builder()
                            .success(true)
                            .message("Join request sent successfully")
                            .build();
                }
            }

            // Create membership request
            GroupTripMember member = GroupTripMember.builder()
                    .groupTripId(groupTripId)
                    .userId(userId)
                    .status(GroupTripMember.MemberStatus.REQUESTED)
                    .joinMessage(request.getJoinMessage())
                    .build();

            groupTripMemberRepository.save(member);

            return ApiResponse.<String>builder()
                    .success(true)
                    .message("Join request sent successfully")
                    .build();

        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .success(false)
                    .error("Failed to join group trip: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponse<String> respondToJoinRequest(UUID groupTripId, UUID memberId, boolean approve, UUID creatorId) {
        try {
            // Check if user is the creator of the group trip
            GroupTrip groupTrip = groupTripRepository.findById(groupTripId)
                    .orElseThrow(() -> new RuntimeException("Group trip not found"));

            if (!groupTrip.getCreatedByUserId().equals(creatorId)) {
                return ApiResponse.<String>builder()
                        .success(false)
                        .error("You are not authorized to manage this group trip")
                        .build();
            }

            // Find the membership request
            GroupTripMember member = groupTripMemberRepository.findByGroupTripIdAndUserId(groupTripId, memberId)
                    .orElseThrow(() -> new RuntimeException("Join request not found"));

            if (member.getStatus() != GroupTripMember.MemberStatus.REQUESTED) {
                return ApiResponse.<String>builder()
                        .success(false)
                        .error("This request has already been processed")
                        .build();
            }

            // Update member status
            member.setStatus(approve ? GroupTripMember.MemberStatus.ACCEPTED : GroupTripMember.MemberStatus.DECLINED);
            groupTripMemberRepository.save(member);

            String message = approve ? "Member approved successfully" : "Member request rejected";
            return ApiResponse.<String>builder()
                    .success(true)
                    .message(message)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .success(false)
                    .error("Failed to respond to join request: " + e.getMessage())
                    .build();
        }
    }

    public ApiResponse<GroupTripResponse> getGroupTripDetails(UUID groupTripId, UUID userId) {
        try {
            GroupTrip groupTrip = groupTripRepository.findById(groupTripId)
                    .orElseThrow(() -> new RuntimeException("Group trip not found"));

            GroupTripResponse response = convertToDetailedResponse(groupTrip, userId);

            return ApiResponse.<GroupTripResponse>builder()
                    .success(true)
                    .data(response)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<GroupTripResponse>builder()
                    .success(false)
                    .error("Failed to fetch group trip details: " + e.getMessage())
                    .build();
        }
    }

    public long getGroupTripCount() {
        return groupTripRepository.count();
    }

    private GroupTripResponse convertToResponse(GroupTrip groupTrip, UUID currentUserId) {
        try {
            // Get creator name
            String creatorName = userRepository.findById(groupTrip.getCreatedByUserId())
                    .map(User::getUsername)
                    .orElse("Unknown User");

            // Get trip plan data
            Object tripPlan = null;
            if (groupTrip.getTripPlanId() != null) {
                try {
                    Optional<TripPlan> tripPlanEntity = tripPlanRepository.findById(groupTrip.getTripPlanId());
                    if (tripPlanEntity.isPresent()) {
                        String tripPlanJson = tripPlanEntity.get().getTripPlan();
                        if (tripPlanJson != null && !tripPlanJson.trim().isEmpty()) {
                            tripPlan = objectMapper.readValue(tripPlanJson, Object.class);
                        }
                    }
                } catch (Exception e) {
                    // If parsing fails, try the old way as fallback
                    if (groupTrip.getTripPlan() != null && !groupTrip.getTripPlan().trim().isEmpty()) {
                        try {
                            tripPlan = objectMapper.readValue(groupTrip.getTripPlan(), Object.class);
                        } catch (Exception e2) {
                            tripPlan = groupTrip.getTripPlan();
                        }
                    }
                }
            }

            // Check if current user has requested to join
            Optional<GroupTripMember> memberRequest = groupTripMemberRepository
                    .findByGroupTripIdAndUserId(groupTrip.getId(), currentUserId);

            // Calculate current members dynamically: 1 (creator) + accepted members
            long acceptedMembersCount = groupTripMemberRepository.countAcceptedMembersByGroupTripId(groupTrip.getId());
            int currentMembers = 1 + (int) acceptedMembersCount; // 1 for creator + accepted members

            return GroupTripResponse.builder()
                    .id(groupTrip.getId())
                    .groupName(groupTrip.getGroupName())
                    .description(groupTrip.getDescription())
                    .maxPeople(groupTrip.getMaxPeople())
                    .meetingPoint(groupTrip.getMeetingPoint())
                    .additionalRequirements(groupTrip.getAdditionalRequirements())
                    .createdByUserId(groupTrip.getCreatedByUserId())
                    .creatorName(creatorName)
                    .tripPlanId(groupTrip.getTripPlanId())
                    .tripPlan(tripPlan)
                    .status(groupTrip.getStatus())
                    .currentMembers(currentMembers)
                    .createdAt(groupTrip.getCreatedAt())
                    .updatedAt(groupTrip.getUpdatedAt())
                    .isCreator(groupTrip.getCreatedByUserId().equals(currentUserId))
                    .hasRequested(memberRequest.isPresent())
                    .memberStatus(memberRequest.map(m -> m.getStatus().toString()).orElse(null))
                    .userJoinStatus(groupTrip.getCreatedByUserId().equals(currentUserId) ? "CREATOR" : 
                                   memberRequest.map(m -> m.getStatus().toString()).orElse("NOT_JOINED"))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert group trip to response", e);
        }
    }

    private GroupTripResponse convertToDetailedResponse(GroupTrip groupTrip, UUID currentUserId) {
        GroupTripResponse response = convertToResponse(groupTrip, currentUserId);
        
        // Add member details if user is the creator
        if (groupTrip.getCreatedByUserId().equals(currentUserId)) {
            List<GroupTripMember> members = groupTripMemberRepository.findByGroupTripIdOrderByJoinedAtDesc(groupTrip.getId());
            
            List<GroupTripMemberResponse> memberResponses = members.stream()
                    .map(this::convertToMemberResponse)
                    .collect(Collectors.toList());
            
            response.setMembers(memberResponses);
        } else {
            // If user is not the creator, still include their own member info for viewing their join message
            Optional<GroupTripMember> currentUserMember = groupTripMemberRepository
                    .findByGroupTripIdAndUserId(groupTrip.getId(), currentUserId);
            
            if (currentUserMember.isPresent()) {
                List<GroupTripMemberResponse> memberResponses = List.of(
                    convertToMemberResponse(currentUserMember.get())
                );
                response.setMembers(memberResponses);
            }
        }
        
        return response;
    }

    private GroupTripMemberResponse convertToMemberResponse(GroupTripMember member) {
        User user = userRepository.findById(member.getUserId()).orElse(null);
        
        return GroupTripMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .userName(user != null ? user.getUsername() : "Unknown User")
                .userEmail(user != null ? user.getEmail() : "Unknown Email")
                .status(member.getStatus())
                .joinedAt(member.getJoinedAt())
                .joinMessage(member.getJoinMessage())
                .build();
    }

    // ===============================
    // GROUP CHAT METHODS
    // ===============================

    @Transactional(readOnly = true)
    public ApiResponse<List<GroupChatMessageResponse>> getGroupChatMessages(UUID groupTripId, UUID currentUserId) {
        try {
            // Verify user has access to this group trip
            if (!hasAccessToGroupTrip(groupTripId, currentUserId)) {
                return ApiResponse.<List<GroupChatMessageResponse>>builder()
                        .success(false)
                        .error("You don't have access to this group chat")
                        .build();
            }

            List<GroupChatMessage> messages = groupChatMessageRepository.findByGroupTripIdOrderByCreatedAtAsc(groupTripId);
            
            List<GroupChatMessageResponse> response = messages.stream()
                    .map(message -> GroupChatMessageResponse.builder()
                            .id(message.getId())
                            .groupTripId(message.getGroupTripId())
                            .senderId(message.getUserId())
                            .senderName(message.getUserName())
                            .message(message.getMessage())
                            .timestamp(message.getCreatedAt())
                            .isCurrentUser(message.getUserId().equals(currentUserId))
                            .build())
                    .collect(Collectors.toList());

            return ApiResponse.<List<GroupChatMessageResponse>>builder()
                    .success(true)
                    .data(response)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<List<GroupChatMessageResponse>>builder()
                    .success(false)
                    .error("Failed to load chat messages: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponse<GroupChatMessageResponse> sendGroupChatMessage(UUID groupTripId, String messageText, UUID currentUserId) {
        try {
            // Verify user has access to this group trip
            if (!hasAccessToGroupTrip(groupTripId, currentUserId)) {
                return ApiResponse.<GroupChatMessageResponse>builder()
                        .success(false)
                        .error("You don't have access to this group chat")
                        .build();
            }

            // Get user info
            Optional<User> userOpt = userRepository.findById(currentUserId);
            String userName = userOpt.map(User::getUsername).orElse("Unknown User");

            // Create and save the message
            GroupChatMessage chatMessage = GroupChatMessage.builder()
                    .groupTripId(groupTripId)
                    .userId(currentUserId)
                    .userName(userName)
                    .message(messageText.trim())
                    .build();

            GroupChatMessage savedMessage = groupChatMessageRepository.save(chatMessage);

            GroupChatMessageResponse response = GroupChatMessageResponse.builder()
                    .id(savedMessage.getId())
                    .groupTripId(savedMessage.getGroupTripId())
                    .senderId(savedMessage.getUserId())
                    .senderName(savedMessage.getUserName())
                    .message(savedMessage.getMessage())
                    .timestamp(savedMessage.getCreatedAt())
                    .isCurrentUser(true)
                    .build();

            return ApiResponse.<GroupChatMessageResponse>builder()
                    .success(true)
                    .data(response)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<GroupChatMessageResponse>builder()
                    .success(false)
                    .error("Failed to send message: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get all members of a group trip (for trip creator to manage)
     */
    public ApiResponse<List<GroupTripMemberResponse>> getGroupTripMembers(UUID groupTripId, UUID creatorId) {
        try {
            // Verify the user is the creator
            GroupTrip groupTrip = groupTripRepository.findById(groupTripId)
                    .orElseThrow(() -> new RuntimeException("Group trip not found"));

            if (!groupTrip.getCreatedByUserId().equals(creatorId)) {
                return ApiResponse.<List<GroupTripMemberResponse>>builder()
                        .success(false)
                        .error("You are not authorized to view group members")
                        .build();
            }

            // Get all members for this group trip
            List<GroupTripMember> members = groupTripMemberRepository.findByGroupTripIdOrderByJoinedAtDesc(groupTripId);
            
            List<GroupTripMemberResponse> memberResponses = members.stream()
                    .map(member -> {
                        // Get user details
                        String userName = "Unknown User";
                        try {
                            Optional<String> userNameOpt = userRepository.findUsernameByUserId(member.getUserId());
                            userName = userNameOpt.orElse("User " + member.getUserId().toString().substring(0, 8));
                        } catch (Exception e) {
                            // Fallback to partial UUID
                            userName = "User " + member.getUserId().toString().substring(0, 8);
                        }

                        return GroupTripMemberResponse.builder()
                                .id(member.getId())
                                .userId(member.getUserId())
                                .userName(userName)
                                .userEmail("") // We can add email lookup later if needed
                                .status(member.getStatus())
                                .joinedAt(member.getJoinedAt())
                                .joinMessage(member.getJoinMessage())
                                .build();
                    })
                    .collect(java.util.stream.Collectors.toList());

            return ApiResponse.<List<GroupTripMemberResponse>>builder()
                    .success(true)
                    .data(memberResponses)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<List<GroupTripMemberResponse>>builder()
                    .success(false)
                    .error("Failed to fetch group members: " + e.getMessage())
                    .build();
        }
    }

    private boolean hasAccessToGroupTrip(UUID groupTripId, UUID userId) {
        // Check if user is the creator
        Optional<GroupTrip> groupTripOpt = groupTripRepository.findById(groupTripId);
        if (groupTripOpt.isPresent() && groupTripOpt.get().getCreatedByUserId().equals(userId)) {
            return true;
        }

        // Check if user is an accepted member
        Optional<GroupTripMember> memberOpt = groupTripMemberRepository.findByGroupTripIdAndUserId(groupTripId, userId);
        return memberOpt.isPresent() && memberOpt.get().getStatus() == GroupTripMember.MemberStatus.ACCEPTED;
    }
}

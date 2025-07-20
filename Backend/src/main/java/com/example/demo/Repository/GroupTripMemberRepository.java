package com.example.demo.Repository;

import com.example.demo.entity.GroupTripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupTripMemberRepository extends JpaRepository<GroupTripMember, UUID> {
    
    List<GroupTripMember> findByGroupTripIdOrderByJoinedAtDesc(UUID groupTripId);
    
    List<GroupTripMember> findByUserIdOrderByJoinedAtDesc(UUID userId);
    
    Optional<GroupTripMember> findByGroupTripIdAndUserId(UUID groupTripId, UUID userId);
    
    @Query("SELECT gtm FROM GroupTripMember gtm WHERE gtm.groupTripId = :groupTripId AND gtm.status = :status")
    List<GroupTripMember> findByGroupTripIdAndStatus(@Param("groupTripId") UUID groupTripId, 
                                                     @Param("status") GroupTripMember.MemberStatus status);
    
    @Query("SELECT COUNT(gtm) FROM GroupTripMember gtm WHERE gtm.groupTripId = :groupTripId AND gtm.status = com.example.demo.entity.GroupTripMember$MemberStatus.ACCEPTED")
    long countAcceptedMembersByGroupTripId(@Param("groupTripId") UUID groupTripId);
    
    boolean existsByGroupTripIdAndUserId(UUID groupTripId, UUID userId);
}

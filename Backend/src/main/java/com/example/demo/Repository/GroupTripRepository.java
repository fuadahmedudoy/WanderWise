package com.example.demo.Repository;

import com.example.demo.entity.GroupTrip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupTripRepository extends JpaRepository<GroupTrip, UUID> {
    
    List<GroupTrip> findByCreatedByUserIdOrderByCreatedAtDesc(UUID createdByUserId);
    
    Page<GroupTrip> findByStatusOrderByCreatedAtDesc(GroupTrip.GroupTripStatus status, Pageable pageable);
    
    @Query("SELECT gt FROM GroupTrip gt WHERE gt.status = :status ORDER BY gt.createdAt DESC")
    List<GroupTrip> findActiveGroupTrips(@Param("status") GroupTrip.GroupTripStatus status);
    
    @Query("SELECT gt FROM GroupTrip gt WHERE gt.createdByUserId != :userId AND gt.status = 'OPEN' " +
           "AND gt.id NOT IN (SELECT gtm.groupTripId FROM GroupTripMember gtm WHERE gtm.userId = :userId " +
           "AND (gtm.status = 'REQUESTED' OR gtm.status = 'ACCEPTED')) " +
           "ORDER BY gt.createdAt DESC")
    List<GroupTrip> findAvailableGroupTripsForUser(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(gt) FROM GroupTrip gt WHERE gt.createdByUserId = :userId")
    long countByCreatedByUserId(@Param("userId") UUID userId);
}

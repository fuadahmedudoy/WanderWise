package com.example.demo.Repository;

import com.example.demo.entity.AcceptedTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AcceptedTripRepository extends JpaRepository<AcceptedTrip, Long> {
    
    /**
     * Find all accepted trips by user ID
     */
    List<AcceptedTrip> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find accepted trips by user ID with pagination
     */
    @Query("SELECT a FROM AcceptedTrip a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    List<AcceptedTrip> findByUserIdWithPagination(@Param("userId") UUID userId);
    
    /**
     * Count accepted trips by user ID
     */
    long countByUserId(UUID userId);
    
    /**
     * Find recent accepted trips by user ID (last 30 days)
     */
    @Query("SELECT a FROM AcceptedTrip a WHERE a.userId = :userId AND a.createdAt >= :thirtyDaysAgo ORDER BY a.createdAt DESC")
    List<AcceptedTrip> findRecentTripsByUserId(@Param("userId") UUID userId, @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
}

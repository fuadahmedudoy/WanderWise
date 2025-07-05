package com.example.demo.Repository;

import com.example.demo.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {
    
    /**
     * Find all trip plans by user ID
     */
    List<TripPlan> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find trip plans by user ID and status
     */
    List<TripPlan> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, TripPlan.TripStatus status);
    
    /**
     * Find trip plans by status
     */
    List<TripPlan> findByStatus(TripPlan.TripStatus status);
    
    /**
     * Find upcoming trips within a date range based on their start date
     */
    @Query(value = "SELECT * FROM trip_plan WHERE status = 'upcoming' AND " +
           "(CAST(trip_plan ->> 'start_date' AS DATE) BETWEEN :startDate AND :endDate OR " +
           "CAST(trip_plan -> 'trip_summary' ->> 'start_date' AS DATE) BETWEEN :startDate AND :endDate)", 
           nativeQuery = true)
    List<TripPlan> findUpcomingTripsInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find trip plans by user ID with pagination
     */
    @Query("SELECT t FROM TripPlan t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    List<TripPlan> findByUserIdWithPagination(@Param("userId") UUID userId);
    
    /**
     * Count trip plans by user ID
     */
    long countByUserId(UUID userId);
    
    /**
     * Count trip plans by user ID and status
     */
    long countByUserIdAndStatus(UUID userId, TripPlan.TripStatus status);
    
    /**
     * Find trip plans created after a specific date
     */
    @Query("SELECT t FROM TripPlan t WHERE t.userId = :userId AND t.createdAt >= :afterDate ORDER BY t.createdAt DESC")
    List<TripPlan> findByUserIdAndCreatedAtAfter(@Param("userId") UUID userId, @Param("afterDate") LocalDateTime afterDate);
}

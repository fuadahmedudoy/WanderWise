package com.example.demo.Repository;

import com.example.demo.entity.TripSpotSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripSpotSuggestionRepository extends JpaRepository<TripSpotSuggestion, UUID> {    @Query(value = "SELECT s.name, s.description, s.lat, s.lon, s.best_time, s.time_needed " +
                   "FROM travel_spots s " +
                   "JOIN travel_cities c ON s.city_id = c.id " +
                   "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :destination, '%')) " +
                   "LIMIT 10", 
           nativeQuery = true)
    List<Object[]> findSpotsByDestination(@Param("destination") String destination);
}

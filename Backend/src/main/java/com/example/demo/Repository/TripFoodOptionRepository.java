package com.example.demo.Repository;

import com.example.demo.entity.TripFoodOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripFoodOptionRepository extends JpaRepository<TripFoodOption, UUID> {
      @Query(value = "SELECT r.name, '', r.lat, r.lon, 0, 0, 'restaurant', '', '', r.avg_cost, 'meal' " +
                   "FROM travel_restaurants r " +
                   "JOIN travel_spots s ON r.spot_id = s.id " +
                   "JOIN travel_cities c ON s.city_id = c.id " +
                   "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :destination, '%')) " +
                   "ORDER BY r.avg_cost ASC " +
                   "LIMIT 15", 
           nativeQuery = true)
    List<Object[]> findFoodOptionsByDestination(@Param("destination") String destination);
}

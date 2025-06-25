package com.example.demo.Repository;

import com.example.demo.entity.TripAccommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripAccommodationRepository extends JpaRepository<TripAccommodation, UUID> {    @Query(value = "SELECT h.name, '', h.lat, h.lon, h.rating, 0, 'hotel', h.contact, '' " +
                   "FROM travel_hotels h " +
                   "JOIN travel_spots s ON h.spot_id = s.id " +
                   "JOIN travel_cities c ON s.city_id = c.id " +
                   "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :destination, '%')) " +
                   "ORDER BY h.rating DESC NULLS LAST " +
                   "LIMIT 10", 
           nativeQuery = true)
    List<Object[]> findAccommodationsByDestination(@Param("destination") String destination);
}

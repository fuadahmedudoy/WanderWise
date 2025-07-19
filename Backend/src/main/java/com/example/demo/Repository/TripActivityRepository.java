package com.example.demo.Repository;

import com.example.demo.entity.TripActivity;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripActivityRepository extends JpaRepository<TripActivity,Long> {
//    boolean existByTripIdAndActivity(Long id,String activity);
    TripActivity findByTripIdAndActivity(Long id,String activity);
   boolean existsByTripId(Long Id);
    List<TripActivity> findAllByTripId(Long tripId);


}

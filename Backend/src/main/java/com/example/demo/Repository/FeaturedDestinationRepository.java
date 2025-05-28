package com.example.demo.Repository;

import com.example.demo.entity.FeaturedDestination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeaturedDestinationRepository extends JpaRepository<FeaturedDestination, UUID> {
    List<FeaturedDestination> findByIsActiveTrue();
}
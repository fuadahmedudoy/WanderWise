package com.example.demo.service;

import com.example.demo.dto.FeaturedDestinationDTO;
import com.example.demo.entity.FeaturedDestination;

import java.util.List;
import java.util.UUID;

public interface FeaturedDestinationService {
    List<FeaturedDestination> getAllFeaturedDestinations();
    //FeaturedDestination getFeaturedDestinationById(UUID id);
//    FeaturedDestination createFeaturedDestination(FeaturedDestinationDTO dto);
//    FeaturedDestination updateFeaturedDestination(UUID id, FeaturedDestinationDTO dto);
//    void deleteFeaturedDestination(UUID id);
}
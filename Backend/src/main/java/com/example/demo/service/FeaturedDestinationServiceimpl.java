package com.example.demo.service;

import com.example.demo.dto.FeaturedDestinationDTO;
import com.example.demo.entity.FeaturedDestination;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.Repository.FeaturedDestinationRepository;
import com.example.demo.service.FeaturedDestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
class FeaturedDestinationServiceImpl implements FeaturedDestinationService {

    private final FeaturedDestinationRepository featuredDestinationRepository;

    @Autowired
    public FeaturedDestinationServiceImpl(FeaturedDestinationRepository featuredDestinationRepository) {
        this.featuredDestinationRepository = featuredDestinationRepository;
    }

    @Override
    public List<FeaturedDestination> getAllFeaturedDestinations() {
        return featuredDestinationRepository.findByIsActiveTrue();
    }

   @Override
   public FeaturedDestination getFeaturedDestinationById(UUID id) {
       return featuredDestinationRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Featured destination not found with id: " + id));
   }

//    @Override
//    @Transactional
//    public FeaturedDestination createFeaturedDestination(FeaturedDestinationDTO dto) {
//        FeaturedDestination destination = FeaturedDestination.builder()
//                .destination(dto.getDestination())
//                .title(dto.getTitle())
//                .imageUrl(dto.getImageUrl())
//                .description(dto.getDescription())
//                .days(dto.getDays())
//                .avgRating(dto.getAvgRating())
//                .reviews(dto.getReviews())
//                .isActive(true)
//                .build();
//
//        return featuredDestinationRepository.save(destination);
//    }
//
//    @Override
//    @Transactional
//    public FeaturedDestination updateFeaturedDestination(UUID id, FeaturedDestinationDTO dto) {
//        FeaturedDestination destination = getFeaturedDestinationById(id);
//
//        destination.setDestination(dto.getDestination());
//        destination.setTitle(dto.getTitle());
//        destination.setImageUrl(dto.getImageUrl());
//        destination.setDescription(dto.getDescription());
//        destination.setDays(dto.getDays());
//        destination.setAvgRating(dto.getAvgRating());
//        destination.setReviews(dto.getReviews());
//
//        return featuredDestinationRepository.save(destination);
//    }
//
//    @Override
//    @Transactional
//    public void deleteFeaturedDestination(UUID id) {
//        FeaturedDestination destination = getFeaturedDestinationById(id);
//        featuredDestinationRepository.delete(destination);
//    }
}
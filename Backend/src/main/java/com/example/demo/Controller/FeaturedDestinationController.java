package com.example.demo.Controller;

import com.example.demo.dto.FeaturedDestinationDTO;
import com.example.demo.service.FeaturedDestinationService;
import com.example.demo.entity.FeaturedDestination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/destinations")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class FeaturedDestinationController {

    private final FeaturedDestinationService featuredDestinationService;

    @Autowired
    public FeaturedDestinationController(FeaturedDestinationService featuredDestinationService) {
        this.featuredDestinationService = featuredDestinationService;
    }

    @GetMapping("/featured")
    public ResponseEntity<List<FeaturedDestination>> getAllFeaturedDestinations() {
        List<FeaturedDestination> destinations = featuredDestinationService.getAllFeaturedDestinations();
        return ResponseEntity.ok(destinations);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<FeaturedDestination> getFeaturedDestinationById(@PathVariable UUID id) {
//        FeaturedDestination destination = featuredDestinationService.getFeaturedDestinationById(id);
//        return ResponseEntity.ok(destination);
//    }
//
//    @PostMapping("/featured")
//    public ResponseEntity<FeaturedDestination> createFeaturedDestination(@RequestBody FeaturedDestinationDTO dto) {
//        FeaturedDestination destination = featuredDestinationService.createFeaturedDestination(dto);
//        return new ResponseEntity<>(destination, HttpStatus.CREATED);
//    }
//
//    @PutMapping("/featured/{id}")
//    public ResponseEntity<FeaturedDestination> updateFeaturedDestination(
//            @PathVariable UUID id,
//            @RequestBody FeaturedDestinationDTO dto) {
//        FeaturedDestination destination = featuredDestinationService.updateFeaturedDestination(id, dto);
//        return ResponseEntity.ok(destination);
//    }
//
//    @DeleteMapping("/featured/{id}")
//    public ResponseEntity<Void> deleteFeaturedDestination(@PathVariable UUID id) {
//        featuredDestinationService.deleteFeaturedDestination(id);
//        return ResponseEntity.noContent().build();
//    }
}
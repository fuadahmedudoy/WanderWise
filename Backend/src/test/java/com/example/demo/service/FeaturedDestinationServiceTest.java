package com.example.demo.service;

import com.example.demo.Repository.FeaturedDestinationRepository;
import com.example.demo.entity.FeaturedDestination;
import com.example.demo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeaturedDestinationServiceTest {

    @Mock
    private FeaturedDestinationRepository featuredDestinationRepository;

    @InjectMocks
    private FeaturedDestinationServiceImpl featuredDestinationService;

    private FeaturedDestination testDestination;
    private UUID testDestinationId;

    @BeforeEach
    void setUp() {
        testDestinationId = UUID.randomUUID();
        testDestination = FeaturedDestination.builder()
                .id(testDestinationId)
                .destination("Cox's Bazar")
                .title("World's Longest Natural Sea Beach")
                .description("World's longest natural sea beach")
                .imageUrl("https://example.com/coxsbazar.jpg")
                .days(3)
                .avgRating(4.5)
                .isActive(true)
                .build();
    }

    @Test
    void testGetAllFeaturedDestinations_Success() {
        // Arrange
        List<FeaturedDestination> destinations = Arrays.asList(testDestination);
        when(featuredDestinationRepository.findByIsActiveTrue()).thenReturn(destinations);

        // Act
        List<FeaturedDestination> result = featuredDestinationService.getAllFeaturedDestinations();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDestination, result.get(0));
        verify(featuredDestinationRepository).findByIsActiveTrue();
    }

    @Test
    void testGetAllFeaturedDestinations_EmptyList() {
        // Arrange
        when(featuredDestinationRepository.findByIsActiveTrue()).thenReturn(Arrays.asList());

        // Act
        List<FeaturedDestination> result = featuredDestinationService.getAllFeaturedDestinations();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(featuredDestinationRepository).findByIsActiveTrue();
    }

    @Test
    void testGetFeaturedDestinationById_Success() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));

        // Act
        FeaturedDestination result = featuredDestinationService.getFeaturedDestinationById(testDestinationId);

        // Assert
        assertNotNull(result);
        assertEquals(testDestination, result);
        assertEquals(testDestinationId, result.getId());
        assertEquals("Cox's Bazar", result.getDestination());
        assertEquals("World's longest natural sea beach", result.getDescription());
        assertEquals("https://example.com/coxsbazar.jpg", result.getImageUrl());
        assertTrue(result.isActive());
        verify(featuredDestinationRepository).findById(testDestinationId);
    }

    @Test
    void testGetFeaturedDestinationById_NotFound() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            featuredDestinationService.getFeaturedDestinationById(testDestinationId);
        });

        assertEquals("Featured destination not found with id: " + testDestinationId, exception.getMessage());
        verify(featuredDestinationRepository).findById(testDestinationId);
    }

    @Test
    void testGetFeaturedDestinationById_NullId() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            featuredDestinationService.getFeaturedDestinationById(null);
        });

        assertEquals("Featured destination not found with id: null", exception.getMessage());
    }

    @Test
    void testRepositoryInteraction_MultipleDestinations() {
        // Arrange
        FeaturedDestination destination1 = FeaturedDestination.builder()
                .id(UUID.randomUUID())
                .destination("Sylhet")
                .title("Tea Gardens Paradise")
                .description("Tea gardens and hills")
                .imageUrl("https://example.com/sylhet.jpg")
                .days(2)
                .avgRating(4.3)
                .isActive(true)
                .build();

        FeaturedDestination destination2 = FeaturedDestination.builder()
                .id(UUID.randomUUID())
                .destination("Rangamati")
                .title("Lake District")
                .description("Lake district")
                .imageUrl("https://example.com/rangamati.jpg")
                .days(3)
                .avgRating(4.6)
                .isActive(true)
                .build();

        List<FeaturedDestination> destinations = Arrays.asList(destination1, destination2);
        when(featuredDestinationRepository.findByIsActiveTrue()).thenReturn(destinations);

        // Act
        List<FeaturedDestination> result = featuredDestinationService.getAllFeaturedDestinations();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(destination1));
        assertTrue(result.contains(destination2));
        verify(featuredDestinationRepository).findByIsActiveTrue();
    }

    @Test
    void testGetFeaturedDestinationById_InactiveDestination() {
        // Arrange
        FeaturedDestination inactiveDestination = FeaturedDestination.builder()
                .id(testDestinationId)
                .destination("Inactive Destination")
                .title("Inactive Title")
                .description("Inactive destination for testing")
                .imageUrl("https://example.com/inactive.jpg")
                .days(1)
                .avgRating(3.0)
                .isActive(false)
                .build();

        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(inactiveDestination));

        // Act
        FeaturedDestination result = featuredDestinationService.getFeaturedDestinationById(testDestinationId);

        // Assert
        assertNotNull(result);
        assertEquals(inactiveDestination, result);
        assertFalse(result.isActive());
        verify(featuredDestinationRepository).findById(testDestinationId);
    }

    @Test
    void testServiceIntegration_GetAllThenGetSpecific() {
        // Arrange
        List<FeaturedDestination> allDestinations = Arrays.asList(testDestination);
        when(featuredDestinationRepository.findByIsActiveTrue()).thenReturn(allDestinations);
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));

        // Act
        List<FeaturedDestination> allResults = featuredDestinationService.getAllFeaturedDestinations();
        FeaturedDestination specificResult = featuredDestinationService.getFeaturedDestinationById(testDestinationId);

        // Assert
        assertEquals(1, allResults.size());
        assertEquals(testDestination, allResults.get(0));
        assertEquals(testDestination, specificResult);
        assertEquals(allResults.get(0).getId(), specificResult.getId());
        
        verify(featuredDestinationRepository).findByIsActiveTrue();
        verify(featuredDestinationRepository).findById(testDestinationId);
    }

    @Test
    void testGetAllFeaturedDestinations_CallsCorrectRepositoryMethod() {
        // Arrange
        when(featuredDestinationRepository.findByIsActiveTrue()).thenReturn(Arrays.asList());

        // Act
        featuredDestinationService.getAllFeaturedDestinations();

        // Assert
        verify(featuredDestinationRepository).findByIsActiveTrue();
        verify(featuredDestinationRepository, never()).findAll();
        verify(featuredDestinationRepository, never()).findById(any());
    }

    @Test
    void testGetFeaturedDestinationById_CallsCorrectRepositoryMethod() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));

        // Act
        featuredDestinationService.getFeaturedDestinationById(testDestinationId);

        // Assert
        verify(featuredDestinationRepository).findById(testDestinationId);
        verify(featuredDestinationRepository, never()).findAll();
        verify(featuredDestinationRepository, never()).findByIsActiveTrue();
    }

    @Test
    void testDestinationProperties_AllFieldsPresent() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));

        // Act
        FeaturedDestination result = featuredDestinationService.getFeaturedDestinationById(testDestinationId);

        // Assert
        assertNotNull(result.getId());
        assertNotNull(result.getDestination());
        assertNotNull(result.getDescription());
        assertNotNull(result.getTitle());
        assertNotNull(result.getImageUrl());
        assertTrue(result.getDestination().length() > 0);
        assertTrue(result.getDescription().length() > 0);
        assertTrue(result.getTitle().length() > 0);
        assertTrue(result.getImageUrl().length() > 0);
    }

    @Test
    void testRepositoryException_GetById() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            featuredDestinationService.getFeaturedDestinationById(testDestinationId);
        });

        assertEquals("Database connection error", exception.getMessage());
        verify(featuredDestinationRepository).findById(testDestinationId);
    }

    @Test
    void testRepositoryException_GetAll() {
        // Arrange
        when(featuredDestinationRepository.findByIsActiveTrue())
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            featuredDestinationService.getAllFeaturedDestinations();
        });

        assertEquals("Database connection error", exception.getMessage());
        verify(featuredDestinationRepository).findByIsActiveTrue();
    }
}

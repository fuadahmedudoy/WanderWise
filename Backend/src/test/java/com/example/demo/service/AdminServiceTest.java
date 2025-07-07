package com.example.demo.service;

import com.example.demo.Repository.FeaturedDestinationRepository;
import com.example.demo.entity.FeaturedDestination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private FeaturedDestinationRepository featuredDestinationRepository;

    @Mock
    private AdminFileStorageService fileStorageService;

    @InjectMocks
    private AdminService adminService;

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
        when(featuredDestinationRepository.findAll()).thenReturn(destinations);

        // Act
        List<FeaturedDestination> result = adminService.getAllFeaturedDestinations();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDestination, result.get(0));
        verify(featuredDestinationRepository).findAll();
    }

    @Test
    void testGetAllFeaturedDestinations_EmptyList() {
        // Arrange
        when(featuredDestinationRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<FeaturedDestination> result = adminService.getAllFeaturedDestinations();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(featuredDestinationRepository).findAll();
    }

    @Test
    void testGetFeaturedDestinationById_Success() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));

        // Act
        Optional<FeaturedDestination> result = adminService.getFeaturedDestinationById(testDestinationId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testDestination, result.get());
        verify(featuredDestinationRepository).findById(testDestinationId);
    }

    @Test
    void testGetFeaturedDestinationById_NotFound() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.empty());

        // Act
        Optional<FeaturedDestination> result = adminService.getFeaturedDestinationById(testDestinationId);

        // Assert
        assertFalse(result.isPresent());
        verify(featuredDestinationRepository).findById(testDestinationId);
    }

    @Test
    void testToggleFeaturedDestinationStatus_Success() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));
        when(featuredDestinationRepository.save(any(FeaturedDestination.class))).thenReturn(testDestination);

        // Act
        FeaturedDestination result = adminService.toggleFeaturedDestinationStatus(testDestinationId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isActive()); // Should be toggled from true to false
        
        ArgumentCaptor<FeaturedDestination> captor = ArgumentCaptor.forClass(FeaturedDestination.class);
        verify(featuredDestinationRepository).save(captor.capture());
        
        FeaturedDestination savedDestination = captor.getValue();
        assertFalse(savedDestination.isActive());
        verify(featuredDestinationRepository).findById(testDestinationId);
    }

    @Test
    void testToggleFeaturedDestinationStatus_FromInactiveToActive() {
        // Arrange
        testDestination.setActive(false);
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));
        when(featuredDestinationRepository.save(any(FeaturedDestination.class))).thenReturn(testDestination);

        // Act
        FeaturedDestination result = adminService.toggleFeaturedDestinationStatus(testDestinationId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isActive()); // Should be toggled from false to true
        
        ArgumentCaptor<FeaturedDestination> captor = ArgumentCaptor.forClass(FeaturedDestination.class);
        verify(featuredDestinationRepository).save(captor.capture());
        
        FeaturedDestination savedDestination = captor.getValue();
        assertTrue(savedDestination.isActive());
    }

    @Test
    void testToggleFeaturedDestinationStatus_DestinationNotFound() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.toggleFeaturedDestinationStatus(testDestinationId);
        });

        assertEquals("Featured destination not found", exception.getMessage());
        verify(featuredDestinationRepository).findById(testDestinationId);
        verify(featuredDestinationRepository, never()).save(any());
    }

    @Test
    void testDeleteFeaturedDestination_Success() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));

        // Act
        adminService.deleteFeaturedDestination(testDestinationId);

        // Assert
        verify(featuredDestinationRepository).findById(testDestinationId);
        verify(featuredDestinationRepository).deleteById(testDestinationId);
    }

    @Test
    void testDeleteFeaturedDestination_DestinationNotFound() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.deleteFeaturedDestination(testDestinationId);
        });

        assertEquals("Featured destination not found", exception.getMessage());
        verify(featuredDestinationRepository).findById(testDestinationId);
        verify(featuredDestinationRepository, never()).delete(any());
    }

    @Test
    void testGetAllFeaturedDestinations_MultipleDestinations() {
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
                .isActive(false)
                .build();

        FeaturedDestination destination3 = FeaturedDestination.builder()
                .id(UUID.randomUUID())
                .destination("Sajek Valley")
                .title("Hill Resort")
                .description("Hill resort")
                .imageUrl("https://example.com/sajek.jpg")
                .days(2)
                .avgRating(4.4)
                .isActive(true)
                .build();

        List<FeaturedDestination> destinations = Arrays.asList(destination1, destination2, destination3);
        when(featuredDestinationRepository.findAll()).thenReturn(destinations);

        // Act
        List<FeaturedDestination> result = adminService.getAllFeaturedDestinations();

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains(destination1));
        assertTrue(result.contains(destination2));
        assertTrue(result.contains(destination3));
        
        // Verify active and inactive destinations are both included
        long activeCount = result.stream().filter(FeaturedDestination::isActive).count();
        long inactiveCount = result.stream().filter(d -> !d.isActive()).count();
        assertEquals(2, activeCount);
        assertEquals(1, inactiveCount);
    }

    @Test
    void testToggleFeaturedDestinationStatus_MultipleToggle() {
        // Arrange - destination starts as active (true)
        testDestination.setActive(true);
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));
        
        // Setup return values for save operations
        FeaturedDestination afterFirstToggle = FeaturedDestination.builder()
                .id(testDestinationId)
                .destination("Cox's Bazar")
                .title("World's Longest Natural Sea Beach")
                .description("World's longest natural sea beach")
                .imageUrl("https://example.com/coxsbazar.jpg")
                .days(3)
                .avgRating(4.5)
                .isActive(false) // After first toggle
                .build();
                
        FeaturedDestination afterSecondToggle = FeaturedDestination.builder()
                .id(testDestinationId)
                .destination("Cox's Bazar")
                .title("World's Longest Natural Sea Beach")
                .description("World's longest natural sea beach")
                .imageUrl("https://example.com/coxsbazar.jpg")
                .days(3)
                .avgRating(4.5)
                .isActive(true) // After second toggle
                .build();

        when(featuredDestinationRepository.save(any(FeaturedDestination.class)))
                .thenReturn(afterFirstToggle)
                .thenReturn(afterSecondToggle);

        // Act - First toggle (true -> false)
        FeaturedDestination result1 = adminService.toggleFeaturedDestinationStatus(testDestinationId);
        
        // Update the mock for second call
        testDestination.setActive(false);
        
        // Act - Second toggle (false -> true)
        FeaturedDestination result2 = adminService.toggleFeaturedDestinationStatus(testDestinationId);

        // Assert
        assertFalse(result1.isActive()); // First toggle should make it inactive
        assertTrue(result2.isActive());  // Second toggle should make it active
        
        verify(featuredDestinationRepository, times(2)).findById(testDestinationId);
        verify(featuredDestinationRepository, times(2)).save(any(FeaturedDestination.class));
    }

    @Test
    void testRepositoryInteraction_ExceptionHandling() {
        // Arrange
        when(featuredDestinationRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.getAllFeaturedDestinations();
        });

        assertEquals("Database connection error", exception.getMessage());
        verify(featuredDestinationRepository).findAll();
    }

    @Test
    void testToggleFeaturedDestinationStatus_SaveException() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));
        when(featuredDestinationRepository.save(any(FeaturedDestination.class)))
                .thenThrow(new RuntimeException("Save operation failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.toggleFeaturedDestinationStatus(testDestinationId);
        });

        assertEquals("Save operation failed", exception.getMessage());
        verify(featuredDestinationRepository).findById(testDestinationId);
        verify(featuredDestinationRepository).save(any(FeaturedDestination.class));
    }

    @Test
    void testDeleteFeaturedDestination_DeleteException() {
        // Arrange
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));
        doThrow(new RuntimeException("Delete operation failed")).when(featuredDestinationRepository).deleteById(testDestinationId);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.deleteFeaturedDestination(testDestinationId);
        });

        assertEquals("Delete operation failed", exception.getMessage());
        verify(featuredDestinationRepository).findById(testDestinationId);
        verify(featuredDestinationRepository).deleteById(testDestinationId);
    }

    @Test
    void testGetFeaturedDestinationById_NullId() {
        // Act
        Optional<FeaturedDestination> result = adminService.getFeaturedDestinationById(null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testToggleFeaturedDestinationStatus_NullId() {
        // Arrange
        when(featuredDestinationRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            adminService.toggleFeaturedDestinationStatus(null);
        });
    }

    @Test
    void testDeleteFeaturedDestination_NullId() {
        // Arrange
        when(featuredDestinationRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            adminService.deleteFeaturedDestination(null);
        });
    }

    @Test
    void testAdminServiceIntegration_CompleteWorkflow() {
        // Arrange
        List<FeaturedDestination> allDestinations = Arrays.asList(testDestination);
        when(featuredDestinationRepository.findAll()).thenReturn(allDestinations);
        when(featuredDestinationRepository.findById(testDestinationId)).thenReturn(Optional.of(testDestination));
        
        // Create toggled version for save operation
        FeaturedDestination toggledDestination = FeaturedDestination.builder()
                .id(testDestinationId)
                .destination("Cox's Bazar")
                .title("World's Longest Natural Sea Beach")
                .description("World's longest natural sea beach")
                .imageUrl("https://example.com/coxsbazar.jpg")
                .days(3)
                .avgRating(4.5)
                .isActive(false) // Toggled to false
                .build();
        when(featuredDestinationRepository.save(any(FeaturedDestination.class))).thenReturn(toggledDestination);

        // Act & Assert - Complete workflow
        // 1. Get all destinations
        List<FeaturedDestination> destinations = adminService.getAllFeaturedDestinations();
        assertEquals(1, destinations.size());
        assertTrue(destinations.get(0).isActive());

        // 2. Get specific destination
        Optional<FeaturedDestination> specificDestination = adminService.getFeaturedDestinationById(testDestinationId);
        assertTrue(specificDestination.isPresent());
        assertTrue(specificDestination.get().isActive());

        // 3. Toggle status
        FeaturedDestination toggled = adminService.toggleFeaturedDestinationStatus(testDestinationId);
        assertFalse(toggled.isActive());

        // 4. Delete destination
        adminService.deleteFeaturedDestination(testDestinationId);

        // Verify all operations
        verify(featuredDestinationRepository).findAll();
        verify(featuredDestinationRepository, times(3)).findById(testDestinationId); // get, toggle, delete
        verify(featuredDestinationRepository).save(any(FeaturedDestination.class));
        verify(featuredDestinationRepository).deleteById(testDestinationId);
    }
}

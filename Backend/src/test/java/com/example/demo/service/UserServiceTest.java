package com.example.demo.service;

import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");
        
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        user.setUserProfile(userProfile);
    }

    @Test
    void registerNewUserAccount_Success() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        // Use ArgumentCaptor to capture the User being saved and return it
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            User savedUser = userCaptor.getValue();
            savedUser.setId(java.util.UUID.randomUUID()); // Simulate setting ID
            return savedUser;
        });

        // When
        User result = userService.registerNewUserAccount(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("USER", result.getRole());
        assertNotNull(result.getUserProfile());
        assertEquals("encodedPassword", result.getPassword());
        
        // Verify the captured user has the correct properties
        User capturedUser = userCaptor.getValue();
        assertEquals("testuser", capturedUser.getUsername());
        assertEquals("test@example.com", capturedUser.getEmail());
        assertEquals("encodedPassword", capturedUser.getPassword());
        assertEquals("USER", capturedUser.getRole());
        assertNotNull(capturedUser.getUserProfile());
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerNewUserAccount_UsernameAlreadyExists() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.ofNullable(user));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.registerNewUserAccount(registerRequest));
        
        assertEquals("Error: Username is already taken!", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUserAccount_EmailAlreadyExists() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.registerNewUserAccount(registerRequest));
        
        assertEquals("Error: Email is already in use!", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewOAuth2User_Success() {
        // Given
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            User savedUser = userCaptor.getValue();
            savedUser.setId(java.util.UUID.randomUUID());
            return savedUser;
        });

        // When
        User result = userService.registerNewOAuth2User("John Doe", "john@example.com");

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getUsername());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("USER", result.getRole());
        assertEquals("", result.getPassword()); // OAuth users have empty password
        assertNotNull(result.getUserProfile());
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findByEmail_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));

        // When
        User result = userService.findByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_NotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        User result = userService.findByEmail("notfound@example.com");

        // Then
        assertNull(result);
        verify(userRepository).findByEmail("notfound@example.com");
    }
}
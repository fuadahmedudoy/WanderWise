package com.example.demo.service;

import com.example.demo.Repository.PendingRegistrationRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.PendingRegistration;
import com.example.demo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OtpService otpService;

    private RegisterRequest registerRequest;
    private PendingRegistration pendingRegistration;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        pendingRegistration = PendingRegistration.builder()
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .otp("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
    }

    @Test
    void generateOtp_ReturnsValidOtp() {
        // When
        String otp = otpService.generateOtp();

        // Then
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void initiateRegistration_Success() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(pendingRegistrationRepository.save(any(PendingRegistration.class))).thenReturn(pendingRegistration);
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        // When
        otpService.initiateRegistration(registerRequest);

        // Then
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(pendingRegistrationRepository).deleteByEmail("test@example.com");
        verify(pendingRegistrationRepository).save(any(PendingRegistration.class));
        verify(emailService).sendOtpEmail(eq("test@example.com"), anyString());
    }

    @Test
    void initiateRegistration_UsernameTaken() {
        // Given
        User existingUser = new User();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(existingUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> otpService.initiateRegistration(registerRequest));
        
        assertEquals("Error: Username is already taken!", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(pendingRegistrationRepository, never()).save(any());
    }

    @Test
    void verifyOtpAndCompleteRegistration_Success() {
        // Given
        String email = "test@example.com";
        String otp = "123456";
        
        when(pendingRegistrationRepository.findByEmail(email)).thenReturn(Optional.of(pendingRegistration));
        when(userRepository.save(any(User.class))).thenReturn(new User());
        doNothing().when(pendingRegistrationRepository).delete(any(PendingRegistration.class));

        // When
        User result = otpService.verifyOtpAndCompleteRegistration(email, otp);

        // Then
        assertNotNull(result);
        verify(pendingRegistrationRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
        verify(pendingRegistrationRepository).delete(pendingRegistration);
    }

    @Test
    void verifyOtpAndCompleteRegistration_InvalidOtp() {
        // Given
        String email = "test@example.com";
        String wrongOtp = "654321";
        
        when(pendingRegistrationRepository.findByEmail(email)).thenReturn(Optional.of(pendingRegistration));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> otpService.verifyOtpAndCompleteRegistration(email, wrongOtp));
        
        assertEquals("Invalid OTP", exception.getMessage());
        verify(pendingRegistrationRepository).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyOtpAndCompleteRegistration_NoPendingRegistration() {
        // Given
        String email = "test@example.com";
        String otp = "123456";
        
        when(pendingRegistrationRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> otpService.verifyOtpAndCompleteRegistration(email, otp));
        
        assertEquals("No pending registration found for this email", exception.getMessage());
    }

    @Test
    void resendOtp_Success() {
        // Given
        String email = "test@example.com";
        when(pendingRegistrationRepository.findByEmail(email)).thenReturn(Optional.of(pendingRegistration));
        when(pendingRegistrationRepository.save(any(PendingRegistration.class))).thenReturn(pendingRegistration);
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        // When
        otpService.resendOtp(email);

        // Then
        verify(pendingRegistrationRepository).findByEmail(email);
        verify(pendingRegistrationRepository).save(pendingRegistration);
        verify(emailService).sendOtpEmail(eq(email), anyString());
    }

    @Test
    void cleanupExpiredRegistrations() {
        // Given
        doNothing().when(pendingRegistrationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));

        // When
        otpService.cleanupExpiredRegistrations();

        // Then
        verify(pendingRegistrationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}
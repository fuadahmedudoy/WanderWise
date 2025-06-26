package com.example.demo.service;

import com.example.demo.Repository.PendingRegistrationRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.PendingRegistration;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }

    @Transactional
    public void initiateRegistration(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.findByUsername(registerRequest.getUsername()) != null) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        try {
            // Delete any existing pending registration for this email
            pendingRegistrationRepository.deleteByEmail(registerRequest.getEmail());
            
            // Flush to ensure delete is completed before insert
            pendingRegistrationRepository.flush();

            // Generate OTP
            String otp = generateOtp();

            // Create pending registration
            PendingRegistration pendingRegistration = PendingRegistration.builder()
                    .email(registerRequest.getEmail())
                    .username(registerRequest.getUsername())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .otp(otp)
                    .build();

            pendingRegistrationRepository.save(pendingRegistration);

            // Send OTP email
            emailService.sendOtpEmail(registerRequest.getEmail(), otp);
            
        } catch (Exception e) {
            // If there's still a constraint violation, try to handle it gracefully
            if (e.getMessage().contains("duplicate key value violates unique constraint")) {
                // Try to find and update the existing record
                Optional<PendingRegistration> existingOptional = pendingRegistrationRepository.findByEmail(registerRequest.getEmail());
                if (existingOptional.isPresent()) {
                    PendingRegistration existing = existingOptional.get();
                    String newOtp = generateOtp();
                    existing.setOtp(newOtp);
                    existing.setUsername(registerRequest.getUsername());
                    existing.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
                    existing.setExpiresAt(LocalDateTime.now().plusMinutes(10));
                    pendingRegistrationRepository.save(existing);
                    emailService.sendOtpEmail(registerRequest.getEmail(), newOtp);
                    return;
                }
            }
            throw new RuntimeException("Failed to initiate registration: " + e.getMessage());
        }
    }

    @Transactional
    public User verifyOtpAndCompleteRegistration(String email, String providedOtp) {
        Optional<PendingRegistration> pendingOptional = pendingRegistrationRepository.findByEmail(email);
        
        if (pendingOptional.isEmpty()) {
            throw new RuntimeException("No pending registration found for this email");
        }

        PendingRegistration pending = pendingOptional.get();

        if (pending.isExpired()) {
            pendingRegistrationRepository.delete(pending);
            throw new RuntimeException("OTP has expired. Please register again.");
        }

        if (!pending.getOtp().equals(providedOtp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // Create the user
        User user = new User();
        user.setUsername(pending.getUsername());
        user.setEmail(pending.getEmail());
        user.setPassword(pending.getPassword()); // Already encoded
        user.setRole("USER");

        // Create and link the profile
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        user.setUserProfile(userProfile);

        User savedUser = userRepository.save(user);

        // Delete the pending registration
        pendingRegistrationRepository.delete(pending);

        return savedUser;
    }

    public void resendOtp(String email) {
        Optional<PendingRegistration> pendingOptional = pendingRegistrationRepository.findByEmail(email);
        
        if (pendingOptional.isEmpty()) {
            throw new RuntimeException("No pending registration found for this email");
        }

        PendingRegistration pending = pendingOptional.get();
        
        // Generate new OTP
        String newOtp = generateOtp();
        pending.setOtp(newOtp);
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        
        pendingRegistrationRepository.save(pending);
        
        // Send new OTP email
        emailService.sendOtpEmail(email, newOtp);
    }

    @Transactional
    public void cleanupExpiredRegistrations() {
        pendingRegistrationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    // Getter for EmailService (for testing purposes)
    public EmailService getEmailService() {
        return emailService;
    }

    @Transactional
    public void cleanupPendingRegistrationByEmail(String email) {
        pendingRegistrationRepository.deleteByEmail(email);
        pendingRegistrationRepository.flush();
    }
} 
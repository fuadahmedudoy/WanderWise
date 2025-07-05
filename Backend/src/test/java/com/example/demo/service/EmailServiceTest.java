package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@wanderwise.com");
    }

    @Test
    void sendOtpEmail_Success() {
        // Given
        String toEmail = "user@example.com";
        String otp = "123456";
        
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendOtpEmail(toEmail, otp);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_HandlesException() {
        // Given
        String toEmail = "user@example.com";
        String otp = "123456";
        
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then (should not throw exception due to error handling)
        emailService.sendOtpEmail(toEmail, otp);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
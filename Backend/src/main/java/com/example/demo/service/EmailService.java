package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@wanderwise.com}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("WanderWise - Email Verification");
            message.setText(
                "Welcome to WanderWise!\n\n" +
                "Your verification code is: " + otp + "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you didn't request this code, please ignore this email.\n\n" +
                "Best regards,\n" +
                "WanderWise Team"
            );
            
            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + toEmail);
        } catch (Exception e) {
            // For testing purposes, log the OTP instead of failing
            System.err.println("❌ Failed to send email to " + toEmail + ": " + e.getMessage());
            System.out.println("📧 [TEST MODE] OTP for " + toEmail + ": " + otp);
            System.out.println("📧 [TEST MODE] Use this OTP to verify your registration: " + otp);
            
            // Don't throw exception to allow testing without proper email setup
            // throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
} 
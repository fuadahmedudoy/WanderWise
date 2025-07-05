package com.example.demo.config;

import com.example.demo.Controller.GoogleOAuth2SuccessHandler;
import com.example.demo.service.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        return mailSender;
    }

    @Bean
    @Primary
    public FileStorageService fileStorageService() {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        when(fileStorageService.save(any(MultipartFile.class))).thenReturn("/uploads/test-file.jpg");
        return fileStorageService;
    }

    @Bean
    @Primary
    public EmailService emailService() {
        EmailService emailService = mock(EmailService.class);
        doNothing().when(emailService).sendOtpEmail(any(String.class), any(String.class));
        return emailService;
    }

    @Bean
    @Primary
    public UserDetailService userDetailService() {
        return mock(UserDetailService.class);
    }

    @Bean
    @Primary
    public TokenBlacklistService tokenBlacklistService() {
        return mock(TokenBlacklistService.class);
    }

    @Bean
    @Primary
    public GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler() {
        return mock(GoogleOAuth2SuccessHandler.class);
    }
}
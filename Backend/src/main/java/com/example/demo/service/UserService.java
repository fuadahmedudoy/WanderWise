package com.example.demo.service;

import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
      public User register(RegisterRequest request) {
        // Check if user with this email already exists
        User existingUserEmail = userRepository.findByEmail(request.getEmail());
        if (existingUserEmail != null) {
            throw new RuntimeException("Email already exists. Please use a different email address or try to log in if you already have an account.");
        }
        
        // Check if username is taken
        User existingUsername = userRepository.findByUsername(request.getUsername());
        if (existingUsername != null) {
            throw new RuntimeException("Username already taken. Please choose a different username.");
        }
        
        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();
                
        // Save user to database
        return userRepository.save(user);
    }
}

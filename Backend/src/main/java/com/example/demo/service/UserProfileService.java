package com.example.demo.service;

import com.example.demo.Repository.UserProfileRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService storageService;

    @Value("${backend-url}")
    private String backendUrl;

    public UserProfileDTO getUserProfile(UUID userId) {
        // FIX: Find the profile, or create a new one if it doesn't exist.
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Cannot create profile for a non-existent user with id: " + userId));

                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    newProfile.setUserId(user.getId());
                    return userProfileRepository.save(newProfile);
                });
        return toDTO(userProfile);
    }

    public UserProfileDTO updateUserProfile(UUID userId, UserProfileDTO userProfileDTO, MultipartFile file) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Cannot update profile for a non-existent user with id: " + userId));

                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    newProfile.setUserId(user.getId());
                    return newProfile;
                });

        if (file != null && !file.isEmpty()) {
            String relativePath = storageService.save(file);
            //String fullUrl = backendUrl + relativePath;
            userProfile.setProfilePictureUrl(relativePath);
        }

        userProfile.setBio(userProfileDTO.getBio());
        userProfile.setFirstName(userProfileDTO.getFirstName());
        userProfile.setLastName(userProfileDTO.getLastName());

        UserProfile savedProfile = userProfileRepository.save(userProfile);
        return toDTO(savedProfile);
    }

    public void createProfileForNewUser(User user) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        userProfile.setUserId(user.getId());
        userProfileRepository.save(userProfile);
    }

    private UserProfileDTO toDTO(UserProfile userProfile) {
        return UserProfileDTO.builder()
                .bio(userProfile.getBio())
                .profilePictureUrl(userProfile.getProfilePictureUrl())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .build();
    }
}
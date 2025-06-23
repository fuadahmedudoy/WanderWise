package com.example.demo.Controller;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileDTO> getUserProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userProfileService.getUserProfile(user.getId()));
    }

    @PutMapping
    public ResponseEntity<UserProfileDTO> updateUserProfile(@AuthenticationPrincipal User user, @RequestBody UserProfileDTO userProfileDTO) {
        return ResponseEntity.ok(userProfileService.updateUserProfile(user.getId(), userProfileDTO));
    }
}
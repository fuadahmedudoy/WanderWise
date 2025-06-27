package com.example.demo.Controller;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @AuthenticationPrincipal User user,
            @RequestPart("profile") UserProfileDTO userProfileDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(userProfileService.updateUserProfile(user.getId(), userProfileDTO, file));
    }
}
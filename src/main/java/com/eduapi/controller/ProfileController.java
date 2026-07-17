package com.eduapi.controller;

import com.eduapi.dto.UserProfileDTO;
import com.eduapi.dto.UserProfileUpdateDTO;
import com.eduapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfileByUsername(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<UserProfileDTO> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(authentication.getName(), dto));
    }
}

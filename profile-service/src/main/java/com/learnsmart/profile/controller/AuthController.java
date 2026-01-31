package com.learnsmart.profile.controller;

import com.learnsmart.profile.dto.ProfileDtos.UserRegistrationRequest;
import com.learnsmart.profile.dto.ProfileDtos.UserProfileResponse;
import com.learnsmart.profile.service.ProfileServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ProfileServiceImpl profileService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.registerUser(request));
    }
}

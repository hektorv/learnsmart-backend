package com.learnsmart.profile.controller;

import com.learnsmart.profile.dto.ProfileDtos.*;
import com.learnsmart.profile.service.ProfileServiceImpl;
import com.learnsmart.profile.service.ProgressService;
import com.learnsmart.profile.dto.ProgressDtos.UserProgressResponse;
import com.learnsmart.profile.dto.ProgressDtos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfileServiceImpl profileService;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private ProfileController profileController;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String authId) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(authId);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCreateProfile() {
        UserRegistrationRequest request = UserRegistrationRequest.builder().email("test@test.com").build();
        UserProfileResponse responseDto = UserProfileResponse.builder().email("test@test.com").build();

        when(profileService.registerUser(request)).thenReturn(responseDto);

        ResponseEntity<UserProfileResponse> response = profileController.createProfile(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void testGetMyProfile_WithHeader() {
        UUID userId = UUID.randomUUID();
        String userIdStr = userId.toString();
        UserProfileResponse responseDto = UserProfileResponse.builder().userId(userId).build();

        when(profileService.getProfile(userId)).thenReturn(responseDto);

        ResponseEntity<UserProfileResponse> response = profileController.getMyProfile(userIdStr);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userId, response.getBody().getUserId());
    }

    @Test
    void testGetMyProfile_WithJwt() {
        String authId = "auth-123";
        UUID userId = UUID.randomUUID();
        mockSecurityContext(authId);

        UserProfileResponse profileByAuth = UserProfileResponse.builder().userId(userId).build();
        // The controller uses getProfileByAuthId to find the UUID, then
        // getProfile(UUID) ? No.
        // Controller calls: profileService.getProfileByAuthId(authUserId).getUserId()
        // Then calls: profileService.getProfile(thatUUID)

        when(profileService.getProfileByAuthId(authId)).thenReturn(profileByAuth);
        when(profileService.getProfile(userId)).thenReturn(profileByAuth);

        ResponseEntity<UserProfileResponse> response = profileController.getMyProfile(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userId, response.getBody().getUserId());
    }

    @Test
    void testGetMyProfile_NoAuth_NoHeader() {
        assertThrows(ResponseStatusException.class, () -> profileController.getMyProfile(null));
    }

    @Test
    void testUpdateMyProfile() {
        UUID userId = UUID.randomUUID();
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder().displayName("New").build();
        UserProfileResponse responseDto = UserProfileResponse.builder().displayName("New").build();

        when(profileService.updateProfile(userId, request)).thenReturn(responseDto);

        ResponseEntity<UserProfileResponse> response = profileController.updateMyProfile(userId.toString(), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New", response.getBody().getDisplayName());
    }

    @Test
    void testGetUserProfile_ById() {
        UUID userId = UUID.randomUUID();
        UserProfileResponse responseDto = UserProfileResponse.builder().userId(userId).build();

        when(profileService.getProfile(userId)).thenReturn(responseDto);

        ResponseEntity<UserProfileResponse> response = profileController.getUserProfile(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userId, response.getBody().getUserId());
    }

    // --- Goals ---

    @Test
    void testGetMyGoals() {
        UUID userId = UUID.randomUUID();
        when(profileService.getUserGoals(userId)).thenReturn(List.of());

        ResponseEntity<List<UserGoalResponse>> response = profileController.getMyGoals(userId.toString());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testCreateGoal() {
        UUID userId = UUID.randomUUID();
        UserGoalCreateRequest request = UserGoalCreateRequest.builder().title("Goal").build();
        UserGoalResponse responseDto = UserGoalResponse.builder().title("Goal").build();

        when(profileService.createGoal(userId, request)).thenReturn(responseDto);

        ResponseEntity<UserGoalResponse> response = profileController.createGoal(userId.toString(), request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testUpdateGoal() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        UserGoalUpdateRequest request = UserGoalUpdateRequest.builder().title("Updated").build();
        UserGoalResponse responseDto = UserGoalResponse.builder().title("Updated").build();

        when(profileService.updateGoal(userId, goalId, request)).thenReturn(responseDto);

        ResponseEntity<UserGoalResponse> response = profileController.updateGoal(userId.toString(), goalId, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteGoal() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        doNothing().when(profileService).deleteGoal(userId, goalId);

        ResponseEntity<Void> response = profileController.deleteGoal(userId.toString(), goalId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    // --- Preferences ---

    @Test
    void testGetMyPreferences() {
        UUID userId = UUID.randomUUID();
        UserStudyPreferencesResponse responseDto = UserStudyPreferencesResponse.builder().build();

        when(profileService.getPreferences(userId)).thenReturn(responseDto);
        ResponseEntity<UserStudyPreferencesResponse> response = profileController.getMyPreferences(userId.toString());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetMyProgress() {
        String xUserId = "user-123";
        UserProgressResponse mockProgress = UserProgressResponse.builder()
                .profile(ProgressDtos.ProfileInfo.builder().userId(UUID.randomUUID().toString()).build())
                .build();

        when(progressService.getConsolidatedProgress(xUserId)).thenReturn(mockProgress);

        ResponseEntity<UserProgressResponse> response = profileController.getMyProgress(xUserId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockProgress, response.getBody());
    }
}

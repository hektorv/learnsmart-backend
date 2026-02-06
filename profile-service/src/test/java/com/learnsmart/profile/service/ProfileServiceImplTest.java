package com.learnsmart.profile.service;

import com.learnsmart.profile.dto.ProfileDtos.*;
import com.learnsmart.profile.model.UserGoal;
import com.learnsmart.profile.model.UserProfile;
import com.learnsmart.profile.model.UserStudyPreferences;
import com.learnsmart.profile.repository.UserGoalRepository;
import com.learnsmart.profile.repository.UserProfileRepository;
import com.learnsmart.profile.repository.UserStudyPreferencesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private UserGoalRepository goalRepository;

    @Mock
    private UserStudyPreferencesRepository preferencesRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private com.learnsmart.profile.client.ContentServiceClient contentClient;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- User Profile Tests ---

    @Test
    void testRegisterUser_Success_WithJwt() {
        // Setup Security Context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("auth-uuid-123");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(profileRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile p = invocation.getArgument(0);
            return p;
        });

        UserProfileResponse response = profileService.registerUser(request);

        assertNotNull(response);
        assertEquals("auth-uuid-123", response.getAuthUserId());
        assertEquals("test@example.com", response.getEmail());
        verify(profileRepository).save(any(UserProfile.class));
    }

    @Test
    void testRegisterUser_Success_NoJwt_Fallback() {
        // Ensure no security context
        SecurityContextHolder.clearContext();

        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(profileRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = profileService.registerUser(request);

        assertNotNull(response);
        assertNotNull(response.getAuthUserId()); // UUID generated
        verify(profileRepository).save(any(UserProfile.class));
    }

    @Test
    void testRegisterUser_EmailExists() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("existing@example.com")
                .build();

        when(profileRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new UserProfile()));

        assertThrows(IllegalArgumentException.class, () -> profileService.registerUser(request));
        verify(profileRepository, never()).save(any());
    }

    @Test
    void testGetProfile_Success() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .email("test@example.com")
                .build();

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));

        UserProfileResponse response = profileService.getProfile(userId);
        assertEquals(userId, response.getUserId());
    }

    @Test
    void testGetProfile_NotFound() {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> profileService.getProfile(userId));
    }

    @Test
    void testGetProfileByAuthId_Success() {
        String authId = "auth-123";
        UserProfile profile = UserProfile.builder().authUserId(authId).build();
        when(profileRepository.findByAuthUserId(authId)).thenReturn(Optional.of(profile));

        UserProfileResponse response = profileService.getProfileByAuthId(authId);
        assertEquals(authId, response.getAuthUserId());
    }

    @Test
    void testGetProfileByAuthId_NotFound() {
        String authId = "auth-123";
        when(profileRepository.findByAuthUserId(authId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> profileService.getProfileByAuthId(authId));
    }

    @Test
    void testUpdateProfile_Success_AllFields() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder().userId(userId).build();

        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .displayName("New Name")
                .birthYear(1990)
                .locale("en-US")
                .timezone("UTC")
                .build();

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        UserProfileResponse response = profileService.updateProfile(userId, request);

        assertEquals("New Name", response.getDisplayName());
        assertEquals(1990, response.getBirthYear());
        assertEquals("en-US", response.getLocale());
        assertEquals("UTC", response.getTimezone());
    }

    @Test
    void testUpdateProfile_NotFound() {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> profileService.updateProfile(userId, UserProfileUpdateRequest.builder().build()));
    }

    // --- User Goal Tests ---

    @Test
    void testCreateGoal() {
        UUID userId = UUID.randomUUID();
        UserGoalCreateRequest request = UserGoalCreateRequest.builder()
                .title("Learn Java")
                .build();

        when(goalRepository.save(any(UserGoal.class))).thenAnswer(i -> {
            UserGoal g = i.getArgument(0);
            g.setId(UUID.randomUUID());
            return g;
        });

        UserGoalResponse response = profileService.createGoal(userId, request);

        assertNotNull(response.getId());
        assertEquals("Learn Java", response.getTitle());
        assertTrue(response.getIsActive());
    }

    @Test
    void testGetUserGoals() {
        UUID userId = UUID.randomUUID();
        UserGoal goal = UserGoal.builder().userId(userId).title("Goal 1").build();
        when(goalRepository.findByUserId(userId)).thenReturn(List.of(goal));

        List<UserGoalResponse> responses = profileService.getUserGoals(userId);
        assertEquals(1, responses.size());
        assertEquals("Goal 1", responses.get(0).getTitle());
    }

    @Test
    void testUpdateGoal_Success() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        UserGoal goal = UserGoal.builder().id(goalId).userId(userId).title("Old").build();

        UserGoalUpdateRequest request = UserGoalUpdateRequest.builder()
                .title("New")
                .description("Desc")
                .domainId(UUID.randomUUID())
                .targetLevel("HIGH")
                .dueDate(LocalDate.now())
                .intensity("HIGH")
                .isActive(false)
                .build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        // Mock Content Client
        when(contentClient.getDomain(any()))
                .thenReturn(new com.learnsmart.profile.client.ContentServiceClient.DomainDto());
        when(goalRepository.save(any(UserGoal.class))).thenAnswer(i -> i.getArgument(0));

        UserGoalResponse response = profileService.updateGoal(userId, goalId, request);

        assertEquals("New", response.getTitle());
        assertEquals("Desc", response.getDescription());
        assertFalse(response.getIsActive());
    }

    @Test
    void testUpdateGoal_NotFound() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> profileService.updateGoal(userId, goalId, UserGoalUpdateRequest.builder().build()));
    }

    @Test
    void testUpdateGoal_WrongUser() {
        UUID userId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        UserGoal goal = UserGoal.builder().id(goalId).userId(otherUser).build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        // Should return empty from filter and thus throw
        assertThrows(IllegalArgumentException.class,
                () -> profileService.updateGoal(userId, goalId, UserGoalUpdateRequest.builder().build()));
    }

    @Test
    void testDeleteGoal_Success() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        UserGoal goal = UserGoal.builder().id(goalId).userId(userId).build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        profileService.deleteGoal(userId, goalId);

        verify(goalRepository).delete(goal);
    }

    @Test
    void testDeleteGoal_NotFound() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> profileService.deleteGoal(userId, goalId));
    }

    // --- Preferences Tests ---

    @Test
    void testGetPreferences_Found() {
        UUID userId = UUID.randomUUID();
        UserStudyPreferences prefs = UserStudyPreferences.builder()
                .userId(userId)
                .hoursPerWeek(10.0)
                .build();
        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(prefs));

        UserStudyPreferencesResponse response = profileService.getPreferences(userId);
        assertEquals(10.0, response.getHoursPerWeek());
    }

    @Test
    void testGetPreferences_Default() {
        UUID userId = UUID.randomUUID();
        when(preferencesRepository.findById(userId)).thenReturn(Optional.empty());

        UserStudyPreferencesResponse response = profileService.getPreferences(userId);
        assertEquals(5.0, response.getHoursPerWeek()); // Default from code
    }

    @Test
    void testUpdatePreferences_CreateNew() {
        UUID userId = UUID.randomUUID();
        UserStudyPreferencesUpdate request = UserStudyPreferencesUpdate.builder()
                .hoursPerWeek(8.0)
                .notificationsEnabled(true)
                .build();

        when(preferencesRepository.findById(userId)).thenReturn(Optional.empty());
        when(preferencesRepository.save(any(UserStudyPreferences.class))).thenAnswer(i -> i.getArgument(0));

        UserStudyPreferencesResponse response = profileService.updatePreferences(userId, request);

        assertEquals(8.0, response.getHoursPerWeek());
        assertTrue(response.getNotificationsEnabled());
    }

    @Test
    void testUpdatePreferences_UpdateExisting() {
        UUID userId = UUID.randomUUID();
        UserStudyPreferences prefs = UserStudyPreferences.builder().userId(userId).hoursPerWeek(2.0).build();

        UserStudyPreferencesUpdate request = UserStudyPreferencesUpdate.builder()
                .hoursPerWeek(20.0)
                .preferredDays(List.of("MONDAY"))
                .preferredSessionMinutes(45)
                .notificationsEnabled(false)
                .build();

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(prefs));
        when(preferencesRepository.save(any(UserStudyPreferences.class))).thenAnswer(i -> i.getArgument(0));

        UserStudyPreferencesResponse response = profileService.updatePreferences(userId, request);

        assertEquals(20.0, response.getHoursPerWeek());
        assertEquals(45, response.getPreferredSessionMinutes());
        assertFalse(response.getNotificationsEnabled());
        assertEquals(1, response.getPreferredDays().size());
    }
}

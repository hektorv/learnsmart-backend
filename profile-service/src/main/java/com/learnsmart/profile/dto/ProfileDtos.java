package com.learnsmart.profile.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileDtos {

    @Data
    @Builder
    public static class UserRegistrationRequest {
        @Email
        @NotBlank
        private String email;

        @NotBlank
        @Size(min = 8)
        private String password;

        @NotBlank
        private String displayName;

        private String locale;
        private String timezone;
        private Map<String, Object> additionalAttributes;
    }

    @Data
    @Builder
    public static class UserProfileResponse {
        private UUID userId;
        private String authUserId;
        private String email;
        private String displayName;
        private Integer birthYear;
        private String locale;
        private String timezone;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class UserProfileUpdateRequest {
        private String displayName;
        private Integer birthYear;
        private String locale;
        private String timezone;
    }

    @Data
    @Builder
    public static class UserGoalCreateRequest {
        @NotBlank
        private String title;
        private String description;
        private String domain;
        private String targetLevel;
        private LocalDate dueDate;
        private String intensity;
    }

    @Data
    @Builder
    public static class UserGoalUpdateRequest {
        private String title;
        private String description;
        private String domain;
        private String targetLevel;
        private LocalDate dueDate;
        private String intensity;
        private Boolean isActive;
        // US-096: Goal completion tracking
        private String status; // ACTIVE, COMPLETED, PAUSED, ABANDONED
        private Integer progressPercentage; // 0-100
    }

    @Data
    @Builder
    public static class UserGoalResponse {
        private UUID id;
        private UUID userId;
        private String title;
        private String description;
        private String domain;
        private String targetLevel;
        private LocalDate dueDate;
        private String intensity;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // US-096: Completion tracking fields
        private OffsetDateTime completedAt;
        private Integer completionPercentage;
        private String status;
    }

    @Data
    @Builder
    public static class UserStudyPreferencesResponse {
        private UUID userId;
        private Double hoursPerWeek;
        private List<String> preferredDays;
        private Integer preferredSessionMinutes;
        private Boolean notificationsEnabled;
    }

    @Data
    @Builder
    public static class UserStudyPreferencesUpdate {
        private Double hoursPerWeek;
        private List<String> preferredDays;
        private Integer preferredSessionMinutes;
        private Boolean notificationsEnabled;
    }
}

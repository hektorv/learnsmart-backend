package com.learnsmart.content.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

public class AiDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateLessonsRequest {
        private String domainId;
        private List<String> skillIds;
        private int nLessons;
        private String level;
        private Double difficulty;
        private String locale;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateLessonsResponse {
        private List<ContentLessonDraft> lessons;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentLessonDraft {
        private String tempId;
        private String title;
        private String description;
        private String body;
        private Integer estimatedMinutes;
        private Double difficulty;
        private String type; // lesson, practice
        private Map<String, Object> metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateSkillsRequest {
        private String topic;
        private String domainId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateSkillsResponse {
        private List<SkillDraft> skills;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillDraft {
        private String code;
        private String name;
        private String description;
        private String level;
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratePrerequisitesRequest {
        private List<SkillDraft> skills;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneratePrerequisitesResponse {
        private List<PrerequisiteLink> prerequisites;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrerequisiteLink {
        private String skillCode;
        private List<String> prerequisiteCodes;
    }

    // US-10-08: AI Assessment Item Generation
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateAssessmentItemsRequest {
        private String contextText;
        private int nItems;
        private String domainId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateAssessmentItemsResponse {
        private List<AssessmentItemDraft> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssessmentItemDraft {
        private String question;
        private List<String> options;
        private int correctIndex;
        private String explanation;
        private String difficulty;
    }

    // US-10-09: AI Skill Tagging
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyzeSkillTagsRequest {
        private String contentText;
        private String domainId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalyzeSkillTagsResponse {
        private List<String> suggestedSkillCodes;
    }
}

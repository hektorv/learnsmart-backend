package com.learnsmart.assessment.dto;

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
    public static class NextItemRequest {
        private String userId;
        private String domain;
        private List<Map<String, Object>> skillState;
        private List<Map<String, Object>> recentHistory;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextItemResponse {
        private Map<String, Object> item;
        private String rationale;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackRequest {
        private String userId;
        private Map<String, Object> item;
        private Map<String, Object> userResponse;
        private List<Map<String, Object>> skillStateBefore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackResponse {
        private Boolean isCorrect;
        private String feedbackMessage;
        private List<String> remediationSuggestions;
    }
}

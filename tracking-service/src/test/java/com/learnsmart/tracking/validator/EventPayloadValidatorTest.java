package com.learnsmart.tracking.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventPayloadValidatorTest {

        private final EventPayloadValidator validator = new EventPayloadValidator();
        private final ObjectMapper objectMapper = new ObjectMapper();

        private String toJson(Map<String, Object> map) {
                try {
                        return objectMapper.writeValueAsString(map);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        @Test
        void testContentStartValid() {
                Map<String, Object> payload = Map.of(
                                "contentItemId", UUID.randomUUID().toString(),
                                "startTime", Instant.now().toString());

                assertDoesNotThrow(() -> validator.validate("CONTENT_START", toJson(payload)));
        }

        @Test
        void testContentStartMissingField() {
                Map<String, Object> payload = Map.of(
                                "contentItemId", UUID.randomUUID().toString()
                // Missing startTime
                );

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> validator.validate("CONTENT_START", toJson(payload)));
                assertTrue(ex.getMessage().contains("Missing required field 'startTime'"));
        }

        @Test
        void testContentCompleteValid() {
                Map<String, Object> payload = Map.of(
                                "contentItemId", UUID.randomUUID().toString(),
                                "completionTime", Instant.now().toString(),
                                "timeSpentMs", 12000L);

                assertDoesNotThrow(() -> validator.validate("CONTENT_COMPLETE", toJson(payload)));
        }

        @Test
        void testContentCompleteInvalidTimeSpent() {
                Map<String, Object> payload = Map.of(
                                "contentItemId", UUID.randomUUID().toString(),
                                "completionTime", Instant.now().toString(),
                                "timeSpentMs", -500L // Invalid: negative
                );

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> validator.validate("CONTENT_COMPLETE", toJson(payload)));
                assertTrue(ex.getMessage().contains("Invalid value for field 'timeSpentMs'"));
        }

        @Test
        void testEvaluationStartValid() {
                Map<String, Object> payload = Map.of(
                                "sessionId", UUID.randomUUID().toString(),
                                "skillId", UUID.randomUUID().toString());

                assertDoesNotThrow(() -> validator.validate("EVALUATION_START", toJson(payload)));
        }

        @Test
        void testEvaluationEndValid() {
                Map<String, Object> payload = Map.of(
                                "sessionId", UUID.randomUUID().toString(),
                                "finalScore", 0.85,
                                "itemsAnswered", 10,
                                "correctCount", 8);

                assertDoesNotThrow(() -> validator.validate("EVALUATION_END", toJson(payload)));
        }

        @Test
        void testEvaluationEndInvalidScore() {
                Map<String, Object> payload = Map.of(
                                "sessionId", UUID.randomUUID().toString(),
                                "finalScore", 1.5 // Invalid: > 1.0
                );

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> validator.validate("EVALUATION_END", toJson(payload)));
                assertTrue(ex.getMessage().contains("Invalid value for field 'finalScore'"));
        }

        @Test
        void testPlanGeneratedValid() {
                Map<String, Object> payload = Map.of(
                                "planId", UUID.randomUUID().toString(),
                                "userId", UUID.randomUUID().toString(),
                                "moduleCount", 5,
                                "estimatedDurationMinutes", 300);

                assertDoesNotThrow(() -> validator.validate("PLAN_GENERATED", toJson(payload)));
        }

        @Test
        void testActivityCompleteValid() {
                Map<String, Object> payload = Map.of(
                                "activityId", UUID.randomUUID().toString(),
                                "planId", UUID.randomUUID().toString(),
                                "completedAt", Instant.now().toString(),
                                "timeSpentMs", 5000L,
                                "score", 0.9);

                assertDoesNotThrow(() -> validator.validate("ACTIVITY_COMPLETE", toJson(payload)));
        }

        @Test
        void testPageViewValid() {
                // PAGE_VIEW has no required fields
                Map<String, Object> payload = Map.of(
                                "page", "/dashboard",
                                "referrer", "/home");

                assertDoesNotThrow(() -> validator.validate("PAGE_VIEW", toJson(payload)));
        }

        @Test
        void testPageViewEmptyPayload() {
                // PAGE_VIEW allows empty payload
                assertDoesNotThrow(() -> validator.validate("PAGE_VIEW", "{}"));
        }

        @Test
        void testUnknownEventType() {
                // Unknown event types are allowed (flexible for future)
                Map<String, Object> payload = Map.of("customField", "value");

                assertDoesNotThrow(() -> validator.validate("CUSTOM_EVENT", toJson(payload)));
        }

        @Test
        void testNullEventType() {
                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> validator.validate(null, "{}"));
                assertTrue(ex.getMessage().contains("Event type cannot be null"));
        }

        @Test
        void testInvalidJSON() {
                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> validator.validate("CONTENT_START", "not-valid-json"));
                assertTrue(ex.getMessage().contains("Invalid JSON payload"));
        }

        @Test
        void testInvalidUUID() {
                Map<String, Object> payload = Map.of(
                                "contentItemId", "not-a-uuid",
                                "startTime", Instant.now().toString());

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> validator.validate("CONTENT_START", toJson(payload)));
                assertTrue(ex.getMessage().contains("Invalid value for field 'contentItemId'"));
        }
}

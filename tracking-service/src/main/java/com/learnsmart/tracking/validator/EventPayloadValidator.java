package com.learnsmart.tracking.validator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Validates event payloads based on event type schemas.
 * Implements US-123: Event Payload Validation.
 */
@Component
public class EventPayloadValidator {

    private static final Map<String, PayloadSchema> SCHEMAS = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // CONTENT_START: requires contentItemId and startTime
        SCHEMAS.put("CONTENT_START", new PayloadSchema(
                Set.of("contentItemId", "startTime"),
                Set.of(),
                Map.of(
                        "contentItemId", EventPayloadValidator::isValidUUID,
                        "startTime", EventPayloadValidator::isValidTimestamp)));

        // CONTENT_COMPLETE: requires contentItemId, completionTime, timeSpentMs
        SCHEMAS.put("CONTENT_COMPLETE", new PayloadSchema(
                Set.of("contentItemId", "completionTime", "timeSpentMs"),
                Set.of(),
                Map.of(
                        "contentItemId", EventPayloadValidator::isValidUUID,
                        "completionTime", EventPayloadValidator::isValidTimestamp,
                        "timeSpentMs", EventPayloadValidator::isPositiveNumber)));

        // EVALUATION_START: requires sessionId and skillId
        SCHEMAS.put("EVALUATION_START", new PayloadSchema(
                Set.of("sessionId", "skillId"),
                Set.of(),
                Map.of(
                        "sessionId", EventPayloadValidator::isValidUUID,
                        "skillId", EventPayloadValidator::isValidUUID)));

        // EVALUATION_END: requires sessionId and finalScore
        SCHEMAS.put("EVALUATION_END", new PayloadSchema(
                Set.of("sessionId", "finalScore"),
                Set.of("itemsAnswered", "correctCount"),
                Map.of(
                        "sessionId", EventPayloadValidator::isValidUUID,
                        "finalScore", EventPayloadValidator::isValidScore)));

        // PLAN_GENERATED: requires planId and userId
        SCHEMAS.put("PLAN_GENERATED", new PayloadSchema(
                Set.of("planId", "userId"),
                Set.of("moduleCount", "estimatedDurationMinutes"),
                Map.of(
                        "planId", EventPayloadValidator::isValidUUID,
                        "userId", EventPayloadValidator::isValidUUID)));

        // ACTIVITY_COMPLETE: requires activityId, planId, completedAt
        SCHEMAS.put("ACTIVITY_COMPLETE", new PayloadSchema(
                Set.of("activityId", "planId", "completedAt"),
                Set.of("timeSpentMs", "score"),
                Map.of(
                        "activityId", EventPayloadValidator::isValidUUID,
                        "planId", EventPayloadValidator::isValidUUID,
                        "completedAt", EventPayloadValidator::isValidTimestamp)));

        // MODULE_STARTED: requires moduleId and planId
        SCHEMAS.put("MODULE_STARTED", new PayloadSchema(
                Set.of("moduleId", "planId"),
                Set.of("startTime"),
                Map.of(
                        "moduleId", EventPayloadValidator::isValidUUID,
                        "planId", EventPayloadValidator::isValidUUID)));

        // MODULE_COMPLETED: requires moduleId, planId, completedAt
        SCHEMAS.put("MODULE_COMPLETED", new PayloadSchema(
                Set.of("moduleId", "planId", "completedAt"),
                Set.of("timeSpentMs", "score", "activitiesCompleted"),
                Map.of(
                        "moduleId", EventPayloadValidator::isValidUUID,
                        "planId", EventPayloadValidator::isValidUUID,
                        "completedAt", EventPayloadValidator::isValidTimestamp)));

        // ACTIVITY_STARTED: requires activityId and planId
        SCHEMAS.put("ACTIVITY_STARTED", new PayloadSchema(
                Set.of("activityId", "planId"),
                Set.of("startTime", "moduleId"),
                Map.of(
                        "activityId", EventPayloadValidator::isValidUUID,
                        "planId", EventPayloadValidator::isValidUUID)));

        // ACTIVITY_COMPLETED: requires activityId, planId, completedAt
        SCHEMAS.put("ACTIVITY_COMPLETED", new PayloadSchema(
                Set.of("activityId", "planId", "completedAt"),
                Set.of("timeSpentMs", "score", "moduleId"),
                Map.of(
                        "activityId", EventPayloadValidator::isValidUUID,
                        "planId", EventPayloadValidator::isValidUUID,
                        "completedAt", EventPayloadValidator::isValidTimestamp)));

        // ASSESSMENT_STARTED: requires sessionId and skillId
        SCHEMAS.put("ASSESSMENT_STARTED", new PayloadSchema(
                Set.of("sessionId", "skillId"),
                Set.of("assessmentType", "startTime"),
                Map.of(
                        "sessionId", EventPayloadValidator::isValidUUID,
                        "skillId", EventPayloadValidator::isValidUUID)));

        // ASSESSMENT_COMPLETED: requires sessionId and finalScore
        SCHEMAS.put("ASSESSMENT_COMPLETED", new PayloadSchema(
                Set.of("sessionId", "finalScore"),
                Set.of("itemsAnswered", "correctCount", "completedAt"),
                Map.of(
                        "sessionId", EventPayloadValidator::isValidUUID,
                        "finalScore", EventPayloadValidator::isValidScore)));

        // PAGE_VIEW: minimal validation (optional fields only)
        SCHEMAS.put("PAGE_VIEW", new PayloadSchema(
                Set.of(),
                Set.of("page", "referrer", "sessionId"),
                Map.of()));
    }

    /**
     * Validates payload JSON string against the schema for the given event type.
     *
     * @param eventType   the type of event
     * @param payloadJson the payload as JSON string
     * @throws IllegalArgumentException if validation fails
     */
    public void validate(String eventType, String payloadJson) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }

        if (payloadJson == null || payloadJson.isBlank()) {
            // Allow empty payloads for events that don't require fields
            PayloadSchema schema = SCHEMAS.get(eventType);
            if (schema != null && !schema.requiredFields.isEmpty()) {
                throw new IllegalArgumentException("Payload cannot be null or empty for event type: " + eventType);
            }
            return;
        }

        // Parse JSON to Map
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON payload: " + e.getMessage());
        }

        PayloadSchema schema = SCHEMAS.get(eventType);
        if (schema == null) {
            // Unknown event types are allowed but logged
            // This provides flexibility for future event types
            return;
        }

        // Check required fields
        for (String requiredField : schema.requiredFields) {
            if (!payload.containsKey(requiredField)) {
                throw new IllegalArgumentException(
                        String.format("Missing required field '%s' for event type '%s'",
                                requiredField, eventType));
            }
        }

        // Validate field types
        for (Map.Entry<String, Predicate<Object>> entry : schema.validators.entrySet()) {
            String field = entry.getKey();
            if (payload.containsKey(field)) {
                Object value = payload.get(field);
                if (!entry.getValue().test(value)) {
                    throw new IllegalArgumentException(
                            String.format("Invalid value for field '%s' in event type '%s': %s",
                                    field, eventType, value));
                }
            }
        }
    }

    // Validation predicates

    private static boolean isValidUUID(Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        String str = (String) value;
        // UUID format: 8-4-4-4-12 hex digits
        return str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    private static boolean isValidTimestamp(Object value) {
        if (value instanceof String) {
            try {
                Instant.parse((String) value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return value instanceof Long || value instanceof Integer;
    }

    private static boolean isPositiveNumber(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue() >= 0;
        }
        return false;
    }

    private static boolean isValidScore(Object value) {
        if (value instanceof Number) {
            double score = ((Number) value).doubleValue();
            return score >= 0.0 && score <= 1.0;
        }
        return false;
    }

    /**
     * Internal class representing a payload schema.
     */
    private static class PayloadSchema {
        final Set<String> requiredFields;
        final Set<String> optionalFields;
        final Map<String, Predicate<Object>> validators;

        PayloadSchema(Set<String> requiredFields,
                Set<String> optionalFields,
                Map<String, Predicate<Object>> validators) {
            this.requiredFields = requiredFields;
            this.optionalFields = optionalFields;
            this.validators = validators;
        }
    }
}

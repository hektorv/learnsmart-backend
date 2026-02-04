package com.learnsmart.planning.service;

import com.learnsmart.planning.model.PlanActivity;
import com.learnsmart.planning.model.PlanModule;
import com.learnsmart.planning.model.LearningPlan;
import com.learnsmart.planning.repository.PlanActivityRepository;
import com.learnsmart.planning.repository.PlanModuleRepository;
import com.learnsmart.planning.repository.LearningPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PlanActivityServiceImplementation implements PlanActivityService {

    private final PlanActivityRepository activityRepository;
    private final PlanModuleRepository moduleRepository;
    private final LearningPlanRepository planRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String TRACKING_SERVICE_URL = "http://localhost:8765/events";

    @Override
    public List<PlanActivity> getActivitiesByModule(UUID moduleId) {
        return activityRepository.findByModuleIdOrderByPositionAsc(moduleId);
    }

    @Override
    @Transactional
    public PlanActivity updateActivityStatus(UUID planId, UUID activityId, String status, Integer overrideMinutes) {
        PlanActivity activity = findById(activityId);

        // Verify plan consistency
        if (!activity.getModule().getPlan().getId().equals(planId)) {
            throw new RuntimeException("Activity does not belong to plan");
        }

        // Validate status transition
        validateStatusTransition(activity.getStatus(), status);

        activity.setStatus(status);
        if (overrideMinutes != null) {
            activity.setOverrideEstimatedMinutes(overrideMinutes);
        }

        PlanActivity saved = activityRepository.save(activity);

        // US-110: Emit tracking event when activity is completed
        if ("completed".equals(status)) {
            emitActivityCompletedEvent(saved);

            // Check and update module/plan completion status
            updateModuleCompletionStatus(activity.getModule());
        }

        return saved;
    }

    @Override
    public PlanActivity findById(UUID activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + activityId));
    }

    /**
     * Validates status transitions (US-110)
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Allow any transition for MVP, but log invalid ones
        // In production, enforce: pending -> in_progress -> completed
        if ("completed".equals(currentStatus) && !"completed".equals(newStatus)) {
            throw new IllegalArgumentException("Cannot change status of completed activity");
        }
    }

    /**
     * Emits ACTIVITY_COMPLETED event to tracking-service (US-110)
     */
    private void emitActivityCompletedEvent(PlanActivity activity) {
        try {
            Map<String, Object> event = Map.of(
                    "userId", activity.getModule().getPlan().getUserId(),
                    "eventType", "ACTIVITY_COMPLETE",
                    "entityType", "PLAN_ACTIVITY",
                    "entityId", activity.getId().toString(),
                    "payload", String.format(
                            "{\"activityId\":\"%s\",\"planId\":\"%s\",\"completedAt\":\"%s\",\"actualMinutesSpent\":%d}",
                            activity.getId(),
                            activity.getModule().getPlan().getId(),
                            activity.getCompletedAt() != null ? activity.getCompletedAt().toString()
                                    : OffsetDateTime.now().toString(),
                            activity.getActualMinutesSpent() != null ? activity.getActualMinutesSpent() : 0));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(event, headers);

            restTemplate.postForEntity(TRACKING_SERVICE_URL, request, Void.class);
        } catch (Exception e) {
            // Graceful degradation: log error but don't fail the transaction
            System.err.println("Failed to emit tracking event: " + e.getMessage());
        }
    }

    /**
     * Updates module and plan completion status if all activities/modules are
     * completed (US-110)
     */
    private void updateModuleCompletionStatus(PlanModule module) {
        // Check if all activities in module are completed
        List<PlanActivity> activities = activityRepository.findByModuleIdOrderByPositionAsc(module.getId());
        boolean allActivitiesCompleted = activities.stream()
                .allMatch(a -> "completed".equals(a.getStatus()));

        if (allActivitiesCompleted && !"completed".equals(module.getStatus())) {
            module.setStatus("completed");
            moduleRepository.save(module);

            // Check if all modules in plan are completed
            updatePlanCompletionStatus(module.getPlan());
        }
    }

    /**
     * Updates plan completion status if all modules are completed (US-110)
     */
    private void updatePlanCompletionStatus(LearningPlan plan) {
        List<PlanModule> modules = plan.getModules();

        // Null-safety check
        if (modules == null || modules.isEmpty()) {
            return;
        }

        boolean allModulesCompleted = modules.stream()
                .allMatch(m -> "completed".equals(m.getStatus()));

        if (allModulesCompleted && !"completed".equals(plan.getStatus())) {
            plan.setStatus("completed");
            planRepository.save(plan);
        }
    }
}

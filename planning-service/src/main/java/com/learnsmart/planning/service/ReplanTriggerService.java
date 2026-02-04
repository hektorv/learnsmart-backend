package com.learnsmart.planning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnsmart.planning.model.LearningPlan;
import com.learnsmart.planning.model.PlanActivity;
import com.learnsmart.planning.model.PlanModule;
import com.learnsmart.planning.model.ReplanTrigger;
import com.learnsmart.planning.repository.PlanActivityRepository;
import com.learnsmart.planning.repository.ReplanTriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReplanTriggerService {

    private final ReplanTriggerRepository triggerRepository;
    private final PlanActivityRepository activityRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Trigger type constants
    private static final String TRIGGER_PROGRESS_DEVIATION = "PROGRESS_DEVIATION";
    private static final String TRIGGER_MASTERY_CHANGE = "MASTERY_CHANGE";
    private static final String TRIGGER_INACTIVITY = "INACTIVITY";
    private static final String TRIGGER_GOAL_UPDATED = "GOAL_UPDATED";

    // Severity constants
    private static final String SEVERITY_LOW = "LOW";
    private static final String SEVERITY_MEDIUM = "MEDIUM";
    private static final String SEVERITY_HIGH = "HIGH";

    // Tracking service URL (should be externalized to config)
    private static final String TRACKING_SERVICE_URL = "http://tracking-service:8080";

    /**
     * Evaluate all trigger conditions for a plan
     */
    @Transactional
    public List<ReplanTrigger> evaluateAllTriggers(LearningPlan plan) {
        List<ReplanTrigger> triggers = new ArrayList<>();

        // Check progress deviation
        ReplanTrigger progressTrigger = evaluateProgressDeviation(plan);
        if (progressTrigger != null) {
            triggers.add(progressTrigger);
        }

        // Check inactivity
        ReplanTrigger inactivityTrigger = evaluateInactivity(plan);
        if (inactivityTrigger != null) {
            triggers.add(inactivityTrigger);
        }

        // Check mastery changes (optional, requires tracking service integration)
        // ReplanTrigger masteryTrigger = evaluateMasteryChanges(plan);
        // if (masteryTrigger != null) {
        // triggers.add(masteryTrigger);
        // }

        return triggers;
    }

    /**
     * Evaluate progress deviation trigger
     * Compares expected progress vs actual progress
     */
    @Transactional
    public ReplanTrigger evaluateProgressDeviation(LearningPlan plan) {
        // Skip if plan is not active
        if (!"active".equals(plan.getStatus())) {
            return null;
        }

        // Calculate expected progress based on time elapsed
        LocalDate startDate = plan.getStartDate();
        LocalDate endDate = plan.getEndDate();
        LocalDate today = LocalDate.now();

        if (startDate == null || endDate == null || today.isBefore(startDate)) {
            return null; // Cannot calculate progress
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        long elapsedDays = ChronoUnit.DAYS.between(startDate, today);

        if (totalDays <= 0) {
            return null; // Invalid date range
        }

        double expectedProgress = Math.min(100.0, (elapsedDays * 100.0) / totalDays);

        // Calculate actual progress
        List<PlanActivity> allActivities = new ArrayList<>();
        if (plan.getModules() != null) {
            for (PlanModule module : plan.getModules()) {
                if (module.getActivities() != null) {
                    allActivities.addAll(module.getActivities());
                }
            }
        }

        if (allActivities.isEmpty()) {
            return null; // No activities to track
        }

        long completedCount = allActivities.stream()
                .filter(a -> "completed".equals(a.getStatus()))
                .count();

        double actualProgress = (completedCount * 100.0) / allActivities.size();

        // Calculate deviation
        double deviation = Math.abs(expectedProgress - actualProgress);

        // Determine if trigger should be created
        String severity = null;
        if (deviation > 50) {
            severity = SEVERITY_HIGH;
        } else if (deviation > 30) {
            severity = SEVERITY_MEDIUM;
        } else if (deviation > 15) {
            severity = SEVERITY_LOW;
        }

        if (severity == null) {
            return null; // Deviation not significant enough
        }

        // Check if similar trigger already exists recently
        if (hasSimilarRecentTrigger(plan.getId(), TRIGGER_PROGRESS_DEVIATION, 7)) {
            return null; // Don't create duplicate triggers
        }

        // Create trigger
        String reason = String.format(
                "Progress deviation detected: Expected %.1f%%, Actual %.1f%%, Deviation %.1f%%",
                expectedProgress, actualProgress, deviation);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("expectedProgress", expectedProgress);
        metadata.put("actualProgress", actualProgress);
        metadata.put("deviation", deviation);
        metadata.put("totalActivities", allActivities.size());
        metadata.put("completedActivities", completedCount);

        return createTrigger(plan, TRIGGER_PROGRESS_DEVIATION, reason, severity, metadata);
    }

    /**
     * Evaluate inactivity trigger
     * Checks if user has been inactive for too long
     */
    @Transactional
    public ReplanTrigger evaluateInactivity(LearningPlan plan) {
        // Skip if plan is not active
        if (!"active".equals(plan.getStatus())) {
            return null;
        }

        try {
            // Query tracking service for last activity
            String url = String.format("%s/tracking/users/%s/events?limit=1&orderBy=timestamp DESC",
                    TRACKING_SERVICE_URL, plan.getUserId());

            // For MVP, we'll use a simpler approach: check last updated timestamp
            // In production, this should query the tracking service
            OffsetDateTime lastActivity = plan.getUpdatedAt();

            if (lastActivity == null) {
                lastActivity = plan.getCreatedAt();
            }

            long daysSinceActivity = ChronoUnit.DAYS.between(lastActivity, OffsetDateTime.now());

            // Determine severity
            String severity = null;
            if (daysSinceActivity > 14) {
                severity = SEVERITY_HIGH;
            } else if (daysSinceActivity > 7) {
                severity = SEVERITY_MEDIUM;
            } else if (daysSinceActivity > 3) {
                severity = SEVERITY_LOW;
            }

            if (severity == null) {
                return null; // Not inactive enough
            }

            // Check for recent similar trigger
            if (hasSimilarRecentTrigger(plan.getId(), TRIGGER_INACTIVITY, 7)) {
                return null;
            }

            String reason = String.format(
                    "User inactive for %d days. Last activity: %s",
                    daysSinceActivity, lastActivity.toLocalDate());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("daysSinceActivity", daysSinceActivity);
            metadata.put("lastActivity", lastActivity.toString());

            return createTrigger(plan, TRIGGER_INACTIVITY, reason, severity, metadata);

        } catch (Exception e) {
            // Graceful degradation if tracking service unavailable
            System.err.println("Failed to check inactivity: " + e.getMessage());
            return null;
        }
    }

    /**
     * Evaluate mastery changes trigger (placeholder for future implementation)
     */
    public ReplanTrigger evaluateMasteryChanges(LearningPlan plan) {
        // TODO: Implement when tracking service mastery endpoint is available
        // This would query tracking-service for recent skill level changes
        // and compare against plan prerequisites
        return null;
    }

    /**
     * Create a trigger suggestion (marks trigger as SUGGESTED and could emit
     * notification)
     */
    @Transactional
    public void createTriggerSuggestion(ReplanTrigger trigger) {
        trigger.setStatus("SUGGESTED");
        trigger.setEvaluatedAt(OffsetDateTime.now());
        triggerRepository.save(trigger);

        // TODO: Emit notification event
        // This could integrate with a notification service to alert the user
        System.out.println("Replan suggestion created for plan: " + trigger.getPlan().getId());
    }

    /**
     * Helper: Create and save a trigger
     */
    private ReplanTrigger createTrigger(
            LearningPlan plan,
            String triggerType,
            String reason,
            String severity,
            Map<String, Object> metadata) {
        try {
            ReplanTrigger trigger = ReplanTrigger.builder()
                    .plan(plan)
                    .triggerType(triggerType)
                    .triggerReason(reason)
                    .severity(severity)
                    .status("PENDING")
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .build();

            return triggerRepository.save(trigger);
        } catch (Exception e) {
            System.err.println("Failed to create trigger: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper: Check if a similar trigger was created recently
     */
    private boolean hasSimilarRecentTrigger(java.util.UUID planId, String triggerType, int daysAgo) {
        return triggerRepository
                .findTopByPlanIdAndTriggerTypeOrderByDetectedAtDesc(planId, triggerType)
                .map(trigger -> {
                    long daysSince = ChronoUnit.DAYS.between(trigger.getDetectedAt(), OffsetDateTime.now());
                    return daysSince < daysAgo;
                })
                .orElse(false);
    }

    /**
     * Find pending triggers for a plan (used by replan workflow)
     */
    public List<ReplanTrigger> findPendingTriggers(java.util.UUID planId) {
        return triggerRepository.findByPlanIdAndStatusOrderByDetectedAtDesc(planId, "PENDING");
    }
}

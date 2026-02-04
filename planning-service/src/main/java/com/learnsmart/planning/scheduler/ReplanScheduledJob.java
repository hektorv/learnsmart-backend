package com.learnsmart.planning.scheduler;

import com.learnsmart.planning.model.LearningPlan;
import com.learnsmart.planning.model.ReplanTrigger;
import com.learnsmart.planning.repository.LearningPlanRepository;
import com.learnsmart.planning.service.ReplanTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReplanScheduledJob {

    private final LearningPlanRepository planRepository;
    private final ReplanTriggerService triggerService;

    private static final int BATCH_SIZE = 100;

    /**
     * Scheduled job to evaluate all active plans for replanning triggers
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void evaluateActivePlansForReplanning() {
        System.out.println("Starting scheduled replan trigger evaluation...");

        int page = 0;
        int totalPlansEvaluated = 0;
        int totalTriggersCreated = 0;
        int totalSuggestionsCreated = 0;

        try {
            Page<LearningPlan> planPage;
            do {
                // Fetch active plans in batches
                planPage = planRepository.findByStatus("active", PageRequest.of(page, BATCH_SIZE));

                for (LearningPlan plan : planPage.getContent()) {
                    try {
                        // Evaluate all triggers for this plan
                        List<ReplanTrigger> triggers = triggerService.evaluateAllTriggers(plan);

                        totalPlansEvaluated++;
                        totalTriggersCreated += triggers.size();

                        // Auto-create suggestions for HIGH severity triggers
                        for (ReplanTrigger trigger : triggers) {
                            if ("HIGH".equals(trigger.getSeverity())) {
                                triggerService.createTriggerSuggestion(trigger);
                                totalSuggestionsCreated++;
                            }
                        }

                    } catch (Exception e) {
                        System.err.println("Error evaluating plan " + plan.getId() + ": " + e.getMessage());
                        // Continue with next plan
                    }
                }

                page++;
            } while (planPage.hasNext());

            System.out.println(String.format(
                    "Replan evaluation complete. Plans: %d, Triggers: %d, Suggestions: %d",
                    totalPlansEvaluated, totalTriggersCreated, totalSuggestionsCreated));

        } catch (Exception e) {
            System.err.println("Scheduled job failed: " + e.getMessage());
        }
    }

    /**
     * Manual trigger for testing (can be called via API endpoint if needed)
     */
    public void runManualEvaluation() {
        evaluateActivePlansForReplanning();
    }
}

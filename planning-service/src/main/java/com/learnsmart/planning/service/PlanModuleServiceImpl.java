package com.learnsmart.planning.service;

import com.learnsmart.planning.model.PlanModule;
import com.learnsmart.planning.repository.PlanModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanModuleServiceImpl implements PlanModuleService {

    private final PlanModuleRepository moduleRepository;
    private final LearningPlanService planService;
    private final com.learnsmart.planning.repository.PlanActivityRepository activityRepository;

    @Override
    public List<PlanModule> getModulesByPlan(UUID planId) {
        return moduleRepository.findByPlanIdOrderByPositionAsc(planId);
    }

    @Override
    @Transactional
    public PlanModule updateModuleStatus(UUID planId, UUID moduleId, String status) {
        PlanModule module = findById(moduleId);
        if (!module.getPlan().getId().equals(planId)) {
            throw new RuntimeException("Module does not belong to plan");
        }
        module.setStatus(status);
        PlanModule saved = moduleRepository.save(module);

        // Trigger completion check
        planService.checkCompletion(planId);

        return saved;
    }

    @Override
    @Transactional
    public com.learnsmart.planning.model.PlanActivity addActivity(UUID planId, UUID moduleId,
            com.learnsmart.planning.dto.PlanDtos.CreateActivityRequest request) {
        PlanModule module = findById(moduleId);
        if (!module.getPlan().getId().equals(planId)) {
            throw new RuntimeException("Module does not belong to plan");
        }

        com.learnsmart.planning.model.PlanActivity activity = new com.learnsmart.planning.model.PlanActivity();
        activity.setModule(module);
        activity.setPosition(request.getPosition());
        activity.setActivityType(request.getActivityType());
        activity.setContentRef(request.getContentRef());
        activity.setEstimatedMinutes(request.getEstimatedMinutes());
        activity.setStatus("pending");

        return activityRepository.save(activity);
    }

    @Override
    public PlanModule findById(UUID moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found: " + moduleId));
    }
}

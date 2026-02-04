package com.learnsmart.planning.service;

import com.learnsmart.planning.model.PlanModule;
import java.util.UUID;
import java.util.List;

public interface PlanModuleService {
    List<PlanModule> getModulesByPlan(UUID planId);

    PlanModule updateModuleStatus(UUID planId, UUID moduleId, String status);

    com.learnsmart.planning.model.PlanActivity addActivity(UUID planId, UUID moduleId,
            com.learnsmart.planning.dto.PlanDtos.CreateActivityRequest request);

    PlanModule findById(UUID moduleId);
}

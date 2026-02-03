package com.learnsmart.planning.controller;

import com.learnsmart.planning.dto.PlanDtos;
import com.learnsmart.planning.model.LearningPlan;
import com.learnsmart.planning.model.PlanActivity;
import com.learnsmart.planning.model.PlanModule;
import com.learnsmart.planning.service.PlanActivityService;
import com.learnsmart.planning.service.PlanModuleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanModuleControllerTest {

    @Mock
    private PlanModuleService moduleService;
    @Mock
    private PlanActivityService activityService;

    @InjectMocks
    private PlanModuleController controller;

    @Test
    void testGetPlanModules() {
        UUID planId = UUID.randomUUID();
        when(moduleService.getModulesByPlan(planId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<PlanDtos.ModuleResponse>> response = controller.getPlanModules(planId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testUpdateModule() {
        UUID planId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();

        LearningPlan plan = new LearningPlan();
        plan.setId(planId);

        PlanModule module = new PlanModule();
        module.setId(moduleId);
        module.setPlan(plan);
        module.setStatus("completed");
        module.setTitle("Module 1");

        PlanDtos.UpdateModuleRequest request = new PlanDtos.UpdateModuleRequest();
        request.setStatus("completed");

        when(moduleService.updateModuleStatus(planId, moduleId, "completed")).thenReturn(module);

        ResponseEntity<PlanDtos.ModuleResponse> response = controller.updateModule(planId, moduleId, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("completed", response.getBody().getStatus());
        assertEquals("Module 1", response.getBody().getTitle());
    }

    @Test
    void testGetPlanActivities_ByModule() {
        UUID planId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();

        when(activityService.getActivitiesByModule(moduleId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<PlanDtos.ActivityResponse>> response = controller.getPlanActivities(planId, moduleId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(activityService).getActivitiesByModule(moduleId);
    }

    @Test
    void testGetPlanActivities_AllForPlan() {
        UUID planId = UUID.randomUUID();

        PlanModule module = new PlanModule();
        module.setActivities(Collections.emptyList());

        when(moduleService.getModulesByPlan(planId)).thenReturn(List.of(module));

        ResponseEntity<List<PlanDtos.ActivityResponse>> response = controller.getPlanActivities(planId, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(moduleService).getModulesByPlan(planId);
    }

    @Test
    void testUpdateActivity() {
        UUID planId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();

        PlanModule module = new PlanModule();
        PlanActivity activity = new PlanActivity();
        activity.setId(activityId);
        activity.setModule(module);
        activity.setStatus("completed");
        activity.setActivityType("lesson");

        PlanDtos.UpdateActivityRequest request = new PlanDtos.UpdateActivityRequest();
        request.setStatus("completed");
        request.setOverrideEstimatedMinutes(30);

        when(activityService.updateActivityStatus(planId, activityId, "completed", 30)).thenReturn(activity);

        ResponseEntity<PlanDtos.ActivityResponse> response = controller.updateActivity(planId, activityId, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("completed", response.getBody().getStatus());
        assertEquals("lesson", response.getBody().getActivityType());
    }
}

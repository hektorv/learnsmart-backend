package com.learnsmart.planning.service;

import com.learnsmart.planning.model.LearningPlan;
import com.learnsmart.planning.model.PlanActivity;
import com.learnsmart.planning.model.PlanModule;
import com.learnsmart.planning.repository.PlanActivityRepository;
import com.learnsmart.planning.repository.PlanModuleRepository;
import com.learnsmart.planning.repository.LearningPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanActivityServiceImplementationTest {

    @Mock
    private PlanActivityRepository activityRepository;

    @Mock
    private PlanModuleRepository moduleRepository;

    @Mock
    private LearningPlanRepository planRepository;

    @InjectMocks
    private PlanActivityServiceImplementation activityService;

    @Test
    void testGetActivitiesByModule() {
        UUID moduleId = UUID.randomUUID();
        when(activityRepository.findByModuleIdOrderByPositionAsc(moduleId)).thenReturn(Collections.emptyList());

        List<PlanActivity> result = activityService.getActivitiesByModule(moduleId);
        assertTrue(result.isEmpty());
        verify(activityRepository).findByModuleIdOrderByPositionAsc(moduleId);
    }

    @Test
    void testUpdateActivityStatus_Success() {
        UUID planId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();

        LearningPlan plan = new LearningPlan();
        plan.setId(planId);
        plan.setUserId(UUID.randomUUID().toString());

        PlanModule module = new PlanModule();
        module.setId(moduleId);
        module.setPlan(plan);

        PlanActivity activity = new PlanActivity();
        activity.setId(activityId);
        activity.setModule(module);
        activity.setStatus("pending");

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(PlanActivity.class))).thenAnswer(i -> i.getArgument(0));
        when(activityRepository.findByModuleIdOrderByPositionAsc(moduleId)).thenReturn(List.of(activity));

        PlanActivity result = activityService.updateActivityStatus(planId, activityId, "completed", null);
        assertEquals("completed", result.getStatus());
        verify(activityRepository).save(activity);
    }

    @Test
    void testUpdateActivityStatus_WithOverrideMinutes() {
        UUID planId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();

        LearningPlan plan = new LearningPlan();
        plan.setId(planId);
        plan.setUserId(UUID.randomUUID().toString());

        PlanModule module = new PlanModule();
        module.setId(moduleId);
        module.setPlan(plan);

        PlanActivity activity = new PlanActivity();
        activity.setId(activityId);
        activity.setModule(module);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(PlanActivity.class))).thenAnswer(i -> i.getArgument(0));
        when(activityRepository.findByModuleIdOrderByPositionAsc(moduleId)).thenReturn(List.of(activity));

        PlanActivity result = activityService.updateActivityStatus(planId, activityId, "completed", 45);
        assertEquals("completed", result.getStatus());
        assertEquals(45, result.getOverrideEstimatedMinutes());
    }

    @Test
    void testUpdateActivityStatus_WrongPlan() {
        UUID planId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        UUID wrongPlanId = UUID.randomUUID();

        LearningPlan plan = new LearningPlan();
        plan.setId(wrongPlanId);

        PlanModule module = new PlanModule();
        module.setPlan(plan);

        PlanActivity activity = new PlanActivity();
        activity.setId(activityId);
        activity.setModule(module);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));

        assertThrows(RuntimeException.class,
                () -> activityService.updateActivityStatus(planId, activityId, "completed", null));
    }

    @Test
    void testFindById_Found() {
        UUID id = UUID.randomUUID();
        PlanActivity activity = new PlanActivity();
        activity.setId(id);

        when(activityRepository.findById(id)).thenReturn(Optional.of(activity));

        PlanActivity result = activityService.findById(id);
        assertEquals(id, result.getId());
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(activityRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> activityService.findById(id));
    }
}

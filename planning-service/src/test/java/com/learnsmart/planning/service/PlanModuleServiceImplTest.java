package com.learnsmart.planning.service;

import com.learnsmart.planning.model.LearningPlan;
import com.learnsmart.planning.model.PlanModule;
import com.learnsmart.planning.repository.PlanModuleRepository;
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
class PlanModuleServiceImplTest {

    @Mock
    private PlanModuleRepository moduleRepository;

    @Mock
    private LearningPlanService planService;

    @InjectMocks
    private PlanModuleServiceImpl moduleService;

    @Test
    void testGetModulesByPlan() {
        UUID planId = UUID.randomUUID();
        when(moduleRepository.findByPlanIdOrderByPositionAsc(planId)).thenReturn(Collections.emptyList());

        List<PlanModule> result = moduleService.getModulesByPlan(planId);
        assertTrue(result.isEmpty());
        verify(moduleRepository).findByPlanIdOrderByPositionAsc(planId);
    }

    @Test
    void testUpdateModuleStatus_Success() {
        UUID planId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();

        LearningPlan plan = new LearningPlan();
        plan.setId(planId);

        PlanModule module = new PlanModule();
        module.setId(moduleId);
        module.setPlan(plan);
        module.setStatus("pending");

        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(moduleRepository.save(any(PlanModule.class))).thenAnswer(i -> i.getArgument(0));

        PlanModule result = moduleService.updateModuleStatus(planId, moduleId, "completed");
        assertEquals("completed", result.getStatus());
        verify(moduleRepository).save(module);
    }

    @Test
    void testUpdateModuleStatus_WrongPlan() {
        UUID planId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        UUID wrongPlanId = UUID.randomUUID();

        LearningPlan plan = new LearningPlan();
        plan.setId(wrongPlanId);

        PlanModule module = new PlanModule();
        module.setId(moduleId);
        module.setPlan(plan);

        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));

        assertThrows(RuntimeException.class, () -> moduleService.updateModuleStatus(planId, moduleId, "completed"));
    }

    @Test
    void testFindById_Found() {
        UUID id = UUID.randomUUID();
        PlanModule module = new PlanModule();
        module.setId(id);

        when(moduleRepository.findById(id)).thenReturn(Optional.of(module));

        PlanModule result = moduleService.findById(id);
        assertEquals(id, result.getId());
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(moduleRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> moduleService.findById(id));
    }
}

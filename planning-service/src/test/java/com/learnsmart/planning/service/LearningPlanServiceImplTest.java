package com.learnsmart.planning.service;

import com.learnsmart.planning.client.Clients;
import com.learnsmart.planning.dto.ExternalDtos;
import com.learnsmart.planning.model.LearningPlan;
import com.learnsmart.planning.model.PlanReplanHistory;
import com.learnsmart.planning.repository.LearningPlanRepository;
import com.learnsmart.planning.repository.PlanReplanHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningPlanServiceImplTest {

    @Mock
    private LearningPlanRepository planRepository;
    @Mock
    private PlanReplanHistoryRepository replanRepository;
    @Mock
    private Clients.ProfileClient profileClient;
    @Mock
    private Clients.ContentClient contentClient;
    @Mock
    private Clients.AiClient aiClient;
    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private LearningPlanServiceImpl planService;

    @Test
    void testCreatePlan_WithModules() {
        LearningPlan plan = new LearningPlan();
        plan.setUserId(UUID.randomUUID().toString());
        plan.setModules(List.of()); // Empty but not null

        when(planRepository.save(any(LearningPlan.class))).thenAnswer(i -> {
            LearningPlan p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        LearningPlan result = planService.createPlan(plan);
        assertNotNull(result.getId());
        verify(planRepository).save(plan);
        // Should not call AI services when modules exist
        verify(aiClient, never()).generatePlan(any());
    }

    @Test
    void testCreatePlan_WithAIGeneration() {
        UUID userId = UUID.randomUUID();
        LearningPlan plan = new LearningPlan();
        plan.setUserId(userId.toString());
        plan.setGoalId("goal-123");

        // Mock profile response
        ExternalDtos.UserProfile profile = new ExternalDtos.UserProfile();
        profile.setUserId(userId.toString());
        when(profileClient.getProfile(userId.toString())).thenReturn(profile);

        // Mock content catalog
        when(contentClient.getContentItems(100)).thenReturn(Collections.emptyList());

        // Mock AI response
        ExternalDtos.GeneratePlanResponse aiResponse = new ExternalDtos.GeneratePlanResponse();
        ExternalDtos.PlanDraft planDraft = new ExternalDtos.PlanDraft();

        ExternalDtos.ModuleDraft moduleDraft = new ExternalDtos.ModuleDraft();
        moduleDraft.setTitle("Module 1");
        moduleDraft.setDescription("Test module");

        ExternalDtos.ActivityDraft activityDraft = new ExternalDtos.ActivityDraft();
        activityDraft.setType("lesson");
        activityDraft.setContentRef("content-123");
        moduleDraft.setActivities(List.of(activityDraft));

        planDraft.setModules(List.of(moduleDraft));
        aiResponse.setPlan(planDraft);

        when(aiClient.generatePlan(any(ExternalDtos.GeneratePlanRequest.class))).thenReturn(aiResponse);
        when(planRepository.save(any(LearningPlan.class))).thenAnswer(i -> {
            LearningPlan p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        LearningPlan result = planService.createPlan(plan);
        assertNotNull(result);
        assertNotNull(result.getModules());
        assertEquals(1, result.getModules().size());
        assertEquals("Module 1", result.getModules().get(0).getTitle());

        verify(profileClient).getProfile(userId.toString());
        verify(contentClient).getContentItems(100);
        verify(aiClient).generatePlan(any());
    }

    @Test
    void testCreatePlan_AIGenerationFailure() {
        UUID userId = UUID.randomUUID();
        LearningPlan plan = new LearningPlan();
        plan.setUserId(userId.toString());

        when(profileClient.getProfile(userId.toString())).thenThrow(new RuntimeException("Profile service down"));
        when(planRepository.save(any(LearningPlan.class))).thenAnswer(i -> {
            LearningPlan p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        // Should not throw, should save empty plan
        LearningPlan result = planService.createPlan(plan);
        assertNotNull(result);
        verify(planRepository).save(plan);
    }

    @Test
    void testFindById_Found() {
        UUID id = UUID.randomUUID();
        LearningPlan plan = new LearningPlan();
        plan.setId(id);

        when(planRepository.findById(id)).thenReturn(Optional.of(plan));

        LearningPlan result = planService.findById(id);
        assertEquals(id, result.getId());
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(planRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> planService.findById(id));
    }

    @Test
    void testFindAll_WithStatus() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<LearningPlan> page = new PageImpl<>(Collections.emptyList());

        when(planRepository.findByStatus("active", pageRequest)).thenReturn(page);

        Page<LearningPlan> result = planService.findAll("active", 0, 20);
        assertTrue(result.isEmpty());
        verify(planRepository).findByStatus("active", pageRequest);
    }

    @Test
    void testFindAll_NoStatus() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<LearningPlan> page = new PageImpl<>(Collections.emptyList());

        when(planRepository.findAll(pageRequest)).thenReturn(page);

        Page<LearningPlan> result = planService.findAll(null, 0, 20);
        assertTrue(result.isEmpty());
        verify(planRepository).findAll(pageRequest);
    }

    @Test
    void testFindByUser_WithStatus() {
        String userId = UUID.randomUUID().toString();
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<LearningPlan> page = new PageImpl<>(Collections.emptyList());

        when(planRepository.findByUserIdAndStatus(userId, "active", pageRequest)).thenReturn(page);

        Page<LearningPlan> result = planService.findByUser(userId, "active", 0, 20);
        assertTrue(result.isEmpty());
        verify(planRepository).findByUserIdAndStatus(userId, "active", pageRequest);
    }

    @Test
    void testUpdatePlan() {
        UUID id = UUID.randomUUID();
        LearningPlan existing = new LearningPlan();
        existing.setId(id);
        existing.setStatus("draft");

        LearningPlan updates = new LearningPlan();
        updates.setStatus("active");
        updates.setHoursPerWeek(new BigDecimal("10"));

        when(planRepository.findById(id)).thenReturn(Optional.of(existing));
        when(planRepository.save(any(LearningPlan.class))).thenAnswer(i -> i.getArgument(0));

        LearningPlan result = planService.updatePlan(id, updates);
        assertEquals("active", result.getStatus());
        assertEquals(new BigDecimal("10"), result.getHoursPerWeek());
    }

    @Test
    void testReplan() {
        UUID id = UUID.randomUUID();
        LearningPlan existing = new LearningPlan();
        existing.setId(id);

        when(planRepository.findById(id)).thenReturn(Optional.of(existing));
        when(replanRepository.save(any(PlanReplanHistory.class))).thenAnswer(i -> i.getArgument(0));
        when(planRepository.save(any(LearningPlan.class))).thenAnswer(i -> i.getArgument(0));

        LearningPlan result = planService.replan(id, "User requested changes", "{}");
        assertNotNull(result);
        verify(replanRepository).save(any(PlanReplanHistory.class));
        verify(planRepository).save(existing);
    }
}

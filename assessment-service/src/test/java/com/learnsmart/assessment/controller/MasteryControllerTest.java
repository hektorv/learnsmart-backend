package com.learnsmart.assessment.controller;

import com.learnsmart.assessment.client.ContentClient;
import com.learnsmart.assessment.dto.MasteryDtos;
import com.learnsmart.assessment.model.UserSkillMastery;
import com.learnsmart.assessment.service.AssessmentSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasteryControllerTest {

    @Mock
    private AssessmentSessionService sessionService;
    @Mock
    private ContentClient contentClient;

    @InjectMocks
    private MasteryController controller;

    @Test
    void testGetUserSkillMastery() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UserSkillMastery.UserSkillMasteryId id = new UserSkillMastery.UserSkillMasteryId(userId, skillId);
        UserSkillMastery mastery = new UserSkillMastery(id, new BigDecimal("0.8"), 5, OffsetDateTime.now());

        MasteryDtos.SkillInfo skillInfo = new MasteryDtos.SkillInfo();
        skillInfo.setName("Java Programming");
        MasteryDtos.DomainInfo domainInfo = new MasteryDtos.DomainInfo();
        domainInfo.setName("Computer Science");
        skillInfo.setDomain(domainInfo);

        when(sessionService.getUserSkillMastery(userId)).thenReturn(List.of(mastery));
        when(contentClient.getSkill(skillId)).thenReturn(skillInfo);

        ResponseEntity<List<MasteryDtos.SkillMasteryEnriched>> response = controller.getUserSkillMastery(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());

        MasteryDtos.SkillMasteryEnriched enriched = response.getBody().get(0);
        assertEquals(skillId, enriched.getSkillId());
        assertEquals(0.8, enriched.getMastery());
        assertEquals("Java Programming", enriched.getSkillName());
        assertEquals("Computer Science", enriched.getDomainName());
    }

    @Test
    void testGetUserSkillMastery_ContentClientFailure() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UserSkillMastery.UserSkillMasteryId id = new UserSkillMastery.UserSkillMasteryId(userId, skillId);
        UserSkillMastery mastery = new UserSkillMastery(id, new BigDecimal("0.8"), 5, OffsetDateTime.now());

        when(sessionService.getUserSkillMastery(userId)).thenReturn(List.of(mastery));
        when(contentClient.getSkill(skillId)).thenThrow(new RuntimeException("Service unavailable"));

        ResponseEntity<List<MasteryDtos.SkillMasteryEnriched>> response = controller.getUserSkillMastery(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        MasteryDtos.SkillMasteryEnriched enriched = response.getBody().get(0);
        assertEquals("Skill " + skillId, enriched.getSkillName()); // Fallback
    }
}

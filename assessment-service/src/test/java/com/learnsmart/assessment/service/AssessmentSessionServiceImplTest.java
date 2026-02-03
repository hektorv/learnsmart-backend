package com.learnsmart.assessment.service;

import com.learnsmart.assessment.model.*;
import com.learnsmart.assessment.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentSessionServiceImplTest {

    @Mock
    private AssessmentSessionRepository sessionRepository;
    @Mock
    private AssessmentItemRepository itemRepository;
    @Mock
    private UserItemResponseRepository responseRepository;
    @Mock
    private UserSkillMasteryRepository masteryRepository;

    @InjectMocks
    private AssessmentSessionServiceImpl sessionService;

    @Test
    void testCreateSession() {
        AssessmentSession session = new AssessmentSession();
        when(sessionRepository.save(any(AssessmentSession.class))).thenReturn(session);

        AssessmentSession result = sessionService.createSession(session);
        assertNotNull(result);
        assertNotNull(result.getStartedAt());
        assertEquals("in_progress", result.getStatus());
        verify(sessionRepository).save(session);
    }

    @Test
    void testGetSession_Found() {
        UUID id = UUID.randomUUID();
        AssessmentSession session = new AssessmentSession();
        session.setId(id);
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        AssessmentSession result = sessionService.getSession(id);
        assertEquals(id, result.getId());
    }

    @Test
    void testGetSession_NotFound() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sessionService.getSession(id));
    }

    @Test
    void testUpdateStatus_Completed() {
        UUID id = UUID.randomUUID();
        AssessmentSession session = new AssessmentSession();
        session.setId(id);
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AssessmentSession.class))).thenAnswer(i -> i.getArgument(0));

        AssessmentSession result = sessionService.updateStatus(id, "completed");
        assertEquals("completed", result.getStatus());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    void testGetNextItem_Found() {
        AssessmentItem item = new AssessmentItem();
        when(itemRepository.findRandomActiveItem()).thenReturn(Optional.of(item));

        AssessmentItem result = sessionService.getNextItem(UUID.randomUUID());
        assertNotNull(result);
    }

    @Test
    void testGetNextItem_NotFound() {
        when(itemRepository.findRandomActiveItem()).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sessionService.getNextItem(UUID.randomUUID()));
    }

    @Test
    void testSubmitResponse_Correct() {
        UUID sessionId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        AssessmentSession session = new AssessmentSession();
        session.setId(sessionId);
        session.setUserId(userId);

        AssessmentItem item = new AssessmentItem();
        item.setId(itemId);
        AssessmentItemOption option = new AssessmentItemOption();
        option.setId(optionId);
        option.setIsCorrect(true);
        item.setOptions(List.of(option));

        AssessmentItemSkill itemSkill = new AssessmentItemSkill();
        AssessmentItemSkill.AssessmentItemSkillId itemSkillId = new AssessmentItemSkill.AssessmentItemSkillId();
        itemSkillId.setSkillId(skillId);
        itemSkill.setId(itemSkillId);
        item.setSkills(List.of(itemSkill));

        SubmitResponseRequest request = new SubmitResponseRequest();
        request.setAssessmentItemId(itemId);
        request.setSelectedOptionId(optionId);
        request.setResponseTimeMs(1000);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(responseRepository.save(any(UserItemResponse.class))).thenAnswer(i -> {
            UserItemResponse r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            r.setCreatedAt(OffsetDateTime.now());
            return r;
        });

        // Mock mastery finding - not present initially
        when(masteryRepository.findById(any(UserSkillMastery.UserSkillMasteryId.class))).thenReturn(Optional.empty());
        when(masteryRepository.save(any(UserSkillMastery.class))).thenAnswer(i -> i.getArgument(0));

        UserItemResponseWithFeedback result = sessionService.submitResponse(sessionId, request);

        assertTrue(result.getIsCorrect());
        assertEquals("Correct!", result.getFeedback());
        assertEquals(1, result.getMasteryUpdates().size());
        UserSkillMastery update = result.getMasteryUpdates().get(0);
        assertEquals(1, update.getAttempts());
        // Initial 0.3 + 0.1 = 0.4
        assertEquals(0, new BigDecimal("0.4").compareTo(update.getMastery()));
    }

    @Test
    void testSubmitResponse_Incorrect() {
        UUID sessionId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AssessmentSession session = new AssessmentSession();
        session.setId(sessionId);
        session.setUserId(userId);

        AssessmentItem item = new AssessmentItem();
        item.setId(itemId);
        AssessmentItemOption option = new AssessmentItemOption();
        option.setId(optionId);
        option.setIsCorrect(false);
        option.setFeedbackTemplate("Wrong answer");
        item.setOptions(List.of(option));

        SubmitResponseRequest request = new SubmitResponseRequest();
        request.setAssessmentItemId(itemId);
        request.setSelectedOptionId(optionId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(responseRepository.save(any(UserItemResponse.class))).thenAnswer(i -> {
            UserItemResponse r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            r.setCreatedAt(OffsetDateTime.now());
            return r;
        });

        UserItemResponseWithFeedback result = sessionService.submitResponse(sessionId, request);

        assertFalse(result.getIsCorrect());
        assertEquals("Wrong answer", result.getFeedback());
    }

    @Test
    void testGetSessionResponses() {
        UUID sessionId = UUID.randomUUID();
        when(responseRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)).thenReturn(Collections.emptyList());

        List<UserItemResponse> result = sessionService.getSessionResponses(sessionId);
        assertTrue(result.isEmpty());
        verify(responseRepository).findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Test
    void testGetUserSkillMastery() {
        UUID userId = UUID.randomUUID();
        when(masteryRepository.findByIdUserId(userId)).thenReturn(Collections.emptyList());

        List<UserSkillMastery> result = sessionService.getUserSkillMastery(userId);
        assertTrue(result.isEmpty());
        verify(masteryRepository).findByIdUserId(userId);
    }
}

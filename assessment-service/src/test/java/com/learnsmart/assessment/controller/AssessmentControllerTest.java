package com.learnsmart.assessment.controller;

import com.learnsmart.assessment.model.*;
import com.learnsmart.assessment.service.AssessmentSessionService;
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
class AssessmentControllerTest {

    @Mock
    private AssessmentSessionService sessionService;

    @InjectMocks
    private AssessmentController controller;

    @Test
    void testCreateSession() {
        AssessmentSession session = new AssessmentSession();
        when(sessionService.createSession(any(AssessmentSession.class))).thenAnswer(i -> {
            AssessmentSession s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ResponseEntity<AssessmentSession> response = controller.createSession(session);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void testGetSession() {
        UUID id = UUID.randomUUID();
        AssessmentSession session = new AssessmentSession();
        session.setId(id);
        when(sessionService.getSession(id)).thenReturn(session);

        ResponseEntity<AssessmentSession> response = controller.getSession(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().getId());
    }

    @Test
    void testUpdateStatus() {
        UUID id = UUID.randomUUID();
        AssessmentSession session = new AssessmentSession();
        session.setId(id);
        session.setStatus("completed");
        when(sessionService.updateStatus(id, "completed")).thenReturn(session);

        ResponseEntity<AssessmentSession> response = controller.updateStatus(id, "completed");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("completed", response.getBody().getStatus());
    }

    @Test
    void testGetNextItem() {
        UUID id = UUID.randomUUID();
        AssessmentItem item = new AssessmentItem();
        when(sessionService.getNextItem(id)).thenReturn(item);

        ResponseEntity<AssessmentItem> response = controller.getNextItem(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testSubmitResponse() {
        UUID id = UUID.randomUUID();
        SubmitResponseRequest request = new SubmitResponseRequest();
        UserItemResponseWithFeedback feedback = new UserItemResponseWithFeedback();
        feedback.setIsCorrect(true);

        when(sessionService.submitResponse(eq(id), any(SubmitResponseRequest.class))).thenReturn(feedback);

        ResponseEntity<UserItemResponseWithFeedback> response = controller.submitResponse(id, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getIsCorrect());
    }

    @Test
    void testGetSessionResponses() {
        UUID id = UUID.randomUUID();
        when(sessionService.getSessionResponses(id)).thenReturn(Collections.emptyList());

        ResponseEntity<List<UserItemResponse>> response = controller.getSessionResponses(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}

package com.learnsmart.tracking.controller;

import com.learnsmart.tracking.model.LearningEvent;
import com.learnsmart.tracking.service.TrackingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingControllerTest {

    @Mock
    private TrackingService service;

    @InjectMocks
    private TrackingController controller;

    @Test
    void testCreateEvent() {
        LearningEvent event = new LearningEvent();
        event.setEventType("content_view");
        event.setPayload(
                "{\"contentItemId\":\"123e4567-e89b-12d3-a456-426614174000\",\"startTime\":\"2024-01-01T10:00:00Z\"}");

        doNothing().when(service).createEvent(any(LearningEvent.class));

        ResponseEntity<?> response = controller.createEvent(event);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(service).createEvent(event);
    }

    @Test
    void testCreateEventWithValidationError() {
        LearningEvent event = new LearningEvent();
        event.setEventType("CONTENT_START");
        event.setPayload("{\"invalid\":\"data\"}");

        doThrow(new IllegalArgumentException("Missing required field 'contentItemId'"))
                .when(service).createEvent(any(LearningEvent.class));

        ResponseEntity<?> response = controller.createEvent(event);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(service).createEvent(event);
    }

    @Test
    void testGetEvents() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<LearningEvent> page = new PageImpl<>(Collections.emptyList());

        when(service.listEvents(userId, null, null, null, null, null, pageable))
                .thenReturn(page);

        Page<LearningEvent> result = controller.getEvents(
                userId, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEventsWithFilters() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now();
        Pageable pageable = PageRequest.of(0, 20);
        Page<LearningEvent> page = new PageImpl<>(Collections.emptyList());

        when(service.listEvents(userId, "content_view", "content", entityId, from, to, pageable))
                .thenReturn(page);

        Page<LearningEvent> result = controller.getEvents(
                userId, "content_view", "content", entityId, from, to, pageable);

        assertNotNull(result);
        verify(service).listEvents(userId, "content_view", "content", entityId, from, to, pageable);
    }
}

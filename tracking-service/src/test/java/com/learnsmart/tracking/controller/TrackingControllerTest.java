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

        when(service.createEvent(any(LearningEvent.class))).thenAnswer(i -> {
            LearningEvent e = i.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        ResponseEntity<LearningEvent> response = controller.createEvent(event);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
        verify(service).createEvent(event);
    }

    @Test
    void testListEvents() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<LearningEvent> page = new PageImpl<>(Collections.emptyList());

        when(service.listEvents(userId, null, null, null, null, null, pageable))
                .thenReturn(page);

        ResponseEntity<Page<LearningEvent>> response = controller.listEvents(
                userId, null, null, null, null, null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testListEvents_WithFilters() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now();
        Pageable pageable = PageRequest.of(0, 20);
        Page<LearningEvent> page = new PageImpl<>(Collections.emptyList());

        when(service.listEvents(userId, "content_view", "content", entityId, from, to, pageable))
                .thenReturn(page);

        ResponseEntity<Page<LearningEvent>> response = controller.listEvents(
                userId, "content_view", "content", entityId, from, to, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).listEvents(userId, "content_view", "content", entityId, from, to, pageable);
    }
}

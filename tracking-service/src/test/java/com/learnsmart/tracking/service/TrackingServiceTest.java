package com.learnsmart.tracking.service;

import com.learnsmart.tracking.model.LearningEvent;
import com.learnsmart.tracking.repository.LearningEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private LearningEventRepository repository;

    @InjectMocks
    private TrackingService trackingService;

    @Test
    void testCreateEvent() {
        LearningEvent event = new LearningEvent();
        event.setEventType("content_view");

        when(repository.save(any(LearningEvent.class))).thenAnswer(i -> {
            LearningEvent e = i.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        LearningEvent result = trackingService.createEvent(event);
        assertNotNull(result.getId());
        verify(repository).save(event);
    }

    @Test
    void testListEvents_AllParameters() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now();
        Pageable pageable = PageRequest.of(0, 20);

        Page<LearningEvent> page = new PageImpl<>(Collections.emptyList());
        when(repository.findEvents(userId, "content_view", "content", entityId, from, to, pageable))
                .thenReturn(page);

        Page<LearningEvent> result = trackingService.listEvents(
                userId, "content_view", "content", entityId, from, to, pageable);

        assertTrue(result.isEmpty());
        verify(repository).findEvents(userId, "content_view", "content", entityId, from, to, pageable);
    }

    @Test
    void testListEvents_NullFilters() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        Page<LearningEvent> page = new PageImpl<>(Collections.emptyList());
        when(repository.findEvents(userId, null, null, null, null, null, pageable))
                .thenReturn(page);

        Page<LearningEvent> result = trackingService.listEvents(
                userId, null, null, null, null, null, pageable);

        assertTrue(result.isEmpty());
        verify(repository).findEvents(userId, null, null, null, null, null, pageable);
    }
}

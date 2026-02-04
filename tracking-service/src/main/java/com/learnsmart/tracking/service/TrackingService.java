package com.learnsmart.tracking.service;

import com.learnsmart.tracking.model.LearningEvent;
import com.learnsmart.tracking.repository.LearningEventRepository;
import com.learnsmart.tracking.validator.EventPayloadValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final LearningEventRepository repository;
    private final EventPayloadValidator payloadValidator;

    /**
     * Asynchronously creates a learning event.
     * US-086: Async Event Tracking
     * US-123: Event Payload Validation
     *
     * @throws IllegalArgumentException if payload validation fails
     */
    @org.springframework.scheduling.annotation.Async
    @Transactional
    public void createEvent(LearningEvent event) {
        // Validate payload before saving (US-123)
        payloadValidator.validate(event.getEventType(), event.getPayload());

        repository.save(event);
    }

    public Page<LearningEvent> listEvents(UUID userId, String eventType, String entityType, UUID entityId,
            OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        return repository.findEvents(userId, eventType, entityType, entityId, from, to, pageable);
    }
}

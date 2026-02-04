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
    private final EventPayloadValidator payloadValidator; // US-123

    /**
     * Creates a new learning event with payload validation.
     * US-123: Validates payload before saving.
     *
     * @throws IllegalArgumentException if payload validation fails
     */
    @Transactional
    public LearningEvent createEvent(LearningEvent event) {
        // US-123: Validate payload before saving
        if (event.getPayload() != null && !event.getPayload().isBlank()) {
            payloadValidator.validate(event.getEventType(), event.getPayload());
        }

        return repository.save(event);
    }

    public Page<LearningEvent> listEvents(UUID userId, String eventType, String entityType, UUID entityId,
            OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        return repository.findEvents(userId, eventType, entityType, entityId, from, to, pageable);
    }
}

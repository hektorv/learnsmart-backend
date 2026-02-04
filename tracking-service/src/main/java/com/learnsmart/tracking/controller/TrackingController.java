package com.learnsmart.tracking.controller;

import com.learnsmart.tracking.model.LearningEvent;
import com.learnsmart.tracking.repository.LearningEventRepository;
import com.learnsmart.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;
    private final LearningEventRepository repository;

    /**
     * Creates a new learning event.
     * US-123: Returns 400 Bad Request if payload validation fails.
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody LearningEvent event) {
        try {
            trackingService.createEvent(event);
            return ResponseEntity.accepted().build();
        } catch (IllegalArgumentException e) {
            // Payload validation failed (US-123)
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", "Invalid event payload",
                            "message", e.getMessage(),
                            "eventType", event.getEventType() != null ? event.getEventType() : "unknown"));
        }
    }

    @GetMapping
    public Page<LearningEvent> getEvents(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            Pageable pageable) {
        return trackingService.listEvents(userId, eventType, entityType, entityId, from, to, pageable);
    }
}

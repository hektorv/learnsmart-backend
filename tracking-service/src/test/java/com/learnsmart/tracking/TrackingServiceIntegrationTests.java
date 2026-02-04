package com.learnsmart.tracking;

import com.learnsmart.tracking.model.LearningEvent;
import com.learnsmart.tracking.repository.LearningEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TrackingServiceIntegrationTests {

    @Autowired
    private LearningEventRepository repository;

    @Test
    void testCreateAndFilterEvents() throws InterruptedException {
        UUID userId = UUID.randomUUID();

        // 1. Create Event 1 (Activity Completed) - synchronous save for test
        LearningEvent event1 = new LearningEvent();
        event1.setUserId(userId);
        event1.setEventType("activity_completed");
        event1.setEntityType("plan_activity");
        event1.setEntityId(UUID.randomUUID());
        event1.setPayload("{\"score\": 10}");

        repository.save(event1); // Direct save for test

        // 2. Create Event 2 (Login)
        LearningEvent event2 = new LearningEvent();
        event2.setUserId(userId);
        event2.setEventType("user_login");
        event2.setOccurredAt(OffsetDateTime.now().minusHours(1)); // Older

        repository.save(event2); // Direct save for test

        // 3. Test Filter by Event Type
        Page<LearningEvent> results = repository.findEvents(
                userId, "activity_completed", null, null, null, null, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("activity_completed", results.getContent().get(0).getEventType());

        // 4. Test Filter by User (All)
        Page<LearningEvent> allUserEvents = repository.findEvents(
                userId, null, null, null, null, null, PageRequest.of(0, 10));
        assertEquals(2, allUserEvents.getTotalElements());
    }
}

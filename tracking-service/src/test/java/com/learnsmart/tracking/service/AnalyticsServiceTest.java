package com.learnsmart.tracking.service;

import com.learnsmart.tracking.dto.DailyActivityResponse;
import com.learnsmart.tracking.dto.UserStatsResponse;
import com.learnsmart.tracking.model.LearningEvent;
import com.learnsmart.tracking.repository.LearningEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private LearningEventRepository eventRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void testCalculateStats_NoEvents() {
        UUID userId = UUID.randomUUID();
        when(eventRepository.findEvents(eq(userId), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        UserStatsResponse stats = analyticsService.calculateStats(userId);

        assertEquals(0.0, stats.totalHours());
        assertEquals(0, stats.currentStreak());
        assertEquals(0, stats.lessonsCompleted());
        assertEquals(0, stats.assessmentsTaken());
        assertEquals(0, stats.totalEvents());
    }

    @Test
    void testCalculateStats_WithEvents() {
        UUID userId = UUID.randomUUID();

        List<LearningEvent> events = new ArrayList<>();

        // Content view event
        LearningEvent event1 = new LearningEvent();
        event1.setUserId(userId);
        event1.setEventType("content_view");
        event1.setEntityId(UUID.randomUUID());
        event1.setPayload("{\"durationSeconds\": 3600}"); // 1 hour
        event1.setOccurredAt(OffsetDateTime.now());
        events.add(event1);

        // Another content view (different entity)
        LearningEvent event2 = new LearningEvent();
        event2.setUserId(userId);
        event2.setEventType("content_view");
        event2.setEntityId(UUID.randomUUID());
        event2.setPayload("{\"durationSeconds\": 1800}"); // 0.5 hours
        event2.setOccurredAt(OffsetDateTime.now());
        events.add(event2);

        // Assessment event
        LearningEvent event3 = new LearningEvent();
        event3.setUserId(userId);
        event3.setEventType("assessment_completed");
        event3.setEntityId(UUID.randomUUID());
        event3.setOccurredAt(OffsetDateTime.now());
        events.add(event3);

        when(eventRepository.findEvents(eq(userId), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(events));

        UserStatsResponse stats = analyticsService.calculateStats(userId);

        assertEquals(1.5, stats.totalHours(), 0.01); // 3600 + 1800 seconds = 1.5 hours
        assertEquals(2, stats.lessonsCompleted()); // 2 unique content views
        assertEquals(1, stats.assessmentsTaken()); // 1 assessment
        assertEquals(3, stats.totalEvents());
    }

    @Test
    void testCalculateStats_Streak() {
        UUID userId = UUID.randomUUID();

        List<LearningEvent> events = new ArrayList<>();

        // Events for today
        LearningEvent today = new LearningEvent();
        today.setUserId(userId);
        today.setEventType("content_view");
        today.setOccurredAt(OffsetDateTime.now());
        events.add(today);

        // Events for yesterday
        LearningEvent yesterday = new LearningEvent();
        yesterday.setUserId(userId);
        yesterday.setEventType("content_view");
        yesterday.setOccurredAt(OffsetDateTime.now().minusDays(1));
        events.add(yesterday);

        // Events for 2 days ago
        LearningEvent twoDaysAgo = new LearningEvent();
        twoDaysAgo.setUserId(userId);
        twoDaysAgo.setEventType("content_view");
        twoDaysAgo.setOccurredAt(OffsetDateTime.now().minusDays(2));
        events.add(twoDaysAgo);

        when(eventRepository.findEvents(eq(userId), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(events));

        UserStatsResponse stats = analyticsService.calculateStats(userId);

        assertEquals(3, stats.currentStreak()); // 3 consecutive days
    }

    @Test
    void testGetActivity() {
        UUID userId = UUID.randomUUID();
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        List<LearningEvent> events = new ArrayList<>();

        // Event on day 1
        LearningEvent event1 = new LearningEvent();
        event1.setUserId(userId);
        event1.setEventType("content_view");
        event1.setPayload("{\"durationSeconds\": 3600}");
        event1.setOccurredAt(from.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime());
        events.add(event1);

        // Another event on day 1
        LearningEvent event2 = new LearningEvent();
        event2.setUserId(userId);
        event2.setEventType("assessment_started");
        event2.setOccurredAt(from.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime().plusHours(2));
        events.add(event2);

        // Event on day 3
        LearningEvent event3 = new LearningEvent();
        event3.setUserId(userId);
        event3.setEventType("content_view");
        event3.setPayload("{\"durationSeconds\": 1800}");
        event3.setOccurredAt(from.plusDays(2).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime());
        events.add(event3);

        when(eventRepository.findEvents(eq(userId), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(events));

        List<DailyActivityResponse> activity = analyticsService.getActivity(userId, from, to);

        assertEquals(2, activity.size()); // 2 days with activity

        // First day should have 2 events
        DailyActivityResponse day1 = activity.stream()
                .filter(a -> a.date().equals(from))
                .findFirst()
                .orElseThrow();
        assertEquals(2, day1.eventCount());
        assertEquals(1.0, day1.hoursStudied(), 0.01); // 3600 seconds

        // Third day should have 1 event
        DailyActivityResponse day3 = activity.stream()
                .filter(a -> a.date().equals(from.plusDays(2)))
                .findFirst()
                .orElseThrow();
        assertEquals(1, day3.eventCount());
        assertEquals(0.5, day3.hoursStudied(), 0.01); // 1800 seconds
    }

    @Test
    void testGetActivity_NoEvents() {
        UUID userId = UUID.randomUUID();
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(eventRepository.findEvents(eq(userId), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        List<DailyActivityResponse> activity = analyticsService.getActivity(userId, from, to);

        assertTrue(activity.isEmpty());
    }
}

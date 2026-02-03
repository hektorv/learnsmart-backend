package com.learnsmart.tracking.controller;

import com.learnsmart.tracking.dto.DailyActivityResponse;
import com.learnsmart.tracking.dto.UserStatsResponse;
import com.learnsmart.tracking.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController controller;

    @Test
    void testGetUserStats() {
        UUID userId = UUID.randomUUID();
        UserStatsResponse stats = new UserStatsResponse(10.5, 5, 20, 3, 100);

        when(analyticsService.calculateStats(userId)).thenReturn(stats);

        ResponseEntity<UserStatsResponse> response = controller.getUserStats(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10.5, response.getBody().totalHours());
        assertEquals(5, response.getBody().currentStreak());
        verify(analyticsService).calculateStats(userId);
    }

    @Test
    void testGetUserActivity_WithDates() {
        UUID userId = UUID.randomUUID();
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        List<DailyActivityResponse> activity = Collections.emptyList();
        when(analyticsService.getActivity(userId, from, to)).thenReturn(activity);

        ResponseEntity<List<DailyActivityResponse>> response = controller.getUserActivity(userId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(analyticsService).getActivity(userId, from, to);
    }

    @Test
    void testGetUserActivity_DefaultDates() {
        UUID userId = UUID.randomUUID();

        List<DailyActivityResponse> activity = Collections.emptyList();
        when(analyticsService.getActivity(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(activity);

        ResponseEntity<List<DailyActivityResponse>> response = controller.getUserActivity(userId, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(analyticsService).getActivity(eq(userId), any(LocalDate.class), any(LocalDate.class));
    }
}

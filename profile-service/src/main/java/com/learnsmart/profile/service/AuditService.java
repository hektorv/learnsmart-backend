package com.learnsmart.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnsmart.profile.model.UserAuditLog;
import com.learnsmart.profile.model.UserGoal;
import com.learnsmart.profile.model.UserProfile;
import com.learnsmart.profile.model.UserStudyPreferences;
import com.learnsmart.profile.repository.UserAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final UserAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Entity type constants
    private static final String ENTITY_TYPE_PROFILE = "PROFILE";
    private static final String ENTITY_TYPE_GOAL = "GOAL";
    private static final String ENTITY_TYPE_PREFERENCES = "PREFERENCES";

    // Action constants
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_UPDATE = "UPDATE";
    private static final String ACTION_DELETE = "DELETE";

    /**
     * Log profile change (CREATE, UPDATE, DELETE)
     */
    @Async
    @Transactional
    public void logProfileChange(
            UUID userId,
            UUID performedBy,
            String action,
            UserProfile oldValue,
            UserProfile newValue,
            HttpServletRequest request) {
        try {
            UserAuditLog log = UserAuditLog.builder()
                    .userId(userId)
                    .performedBy(performedBy)
                    .entityType(ENTITY_TYPE_PROFILE)
                    .entityId(userId) // For profiles, entityId = userId
                    .action(action)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .ipAddress(extractIpAddress(request))
                    .userAgent(extractUserAgent(request))
                    .timestamp(OffsetDateTime.now())
                    .build();

            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to serialize profile for audit log: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    /**
     * Log goal change (CREATE, UPDATE, DELETE)
     */
    @Async
    @Transactional
    public void logGoalChange(
            UUID userId,
            UUID performedBy,
            String action,
            UserGoal oldValue,
            UserGoal newValue,
            HttpServletRequest request) {
        try {
            UUID entityId = oldValue != null ? oldValue.getId() : (newValue != null ? newValue.getId() : null);

            UserAuditLog log = UserAuditLog.builder()
                    .userId(userId)
                    .performedBy(performedBy)
                    .entityType(ENTITY_TYPE_GOAL)
                    .entityId(entityId)
                    .action(action)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .ipAddress(extractIpAddress(request))
                    .userAgent(extractUserAgent(request))
                    .timestamp(OffsetDateTime.now())
                    .build();

            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize goal for audit log: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    /**
     * Log preferences change (CREATE, UPDATE, DELETE)
     */
    @Async
    @Transactional
    public void logPreferencesChange(
            UUID userId,
            UUID performedBy,
            String action,
            UserStudyPreferences oldValue,
            UserStudyPreferences newValue,
            HttpServletRequest request) {
        try {
            UserAuditLog log = UserAuditLog.builder()
                    .userId(userId)
                    .performedBy(performedBy)
                    .entityType(ENTITY_TYPE_PREFERENCES)
                    .entityId(userId) // For preferences, entityId = userId
                    .action(action)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .ipAddress(extractIpAddress(request))
                    .userAgent(extractUserAgent(request))
                    .timestamp(OffsetDateTime.now())
                    .build();

            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize preferences for audit log: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    /**
     * Get audit trail for a specific user
     */
    @Transactional(readOnly = true)
    public List<UserAuditLog> getAuditTrail(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    /**
     * Get audit trail for a specific entity
     */
    @Transactional(readOnly = true)
    public List<UserAuditLog> getEntityAuditTrail(String entityType, UUID entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable);
    }

    /**
     * Get audit trail within a date range
     */
    @Transactional(readOnly = true)
    public List<UserAuditLog> getAuditTrailByDateRange(
            UUID userId,
            OffsetDateTime startDate,
            OffsetDateTime endDate) {
        return auditLogRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(userId, startDate, endDate);
    }

    /**
     * Extract IP address from HTTP request
     */
    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // Check for X-Forwarded-For header (proxy/load balancer)
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // Handle multiple IPs in X-Forwarded-For (take first one)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    /**
     * Extract User-Agent from HTTP request
     */
    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }
}

package com.learnsmart.profile.controller;

import com.learnsmart.profile.model.UserAuditLog;
import com.learnsmart.profile.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /**
     * Get audit trail for a specific user
     * US-094: User Audit Trail
     */
    @GetMapping("/{userId}/audit")
    public ResponseEntity<List<UserAuditLog>> getUserAuditTrail(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OffsetDateTime startDate,
            @RequestParam(required = false) OffsetDateTime endDate) {
        List<UserAuditLog> auditLogs;

        if (startDate != null && endDate != null) {
            auditLogs = auditService.getAuditTrailByDateRange(userId, startDate, endDate);
        } else {
            auditLogs = auditService.getAuditTrail(userId, page, size);
        }

        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit trail for a specific goal
     * US-094: User Audit Trail
     */
    @GetMapping("/{userId}/goals/{goalId}/audit")
    public ResponseEntity<List<UserAuditLog>> getGoalAuditTrail(
            @PathVariable UUID userId,
            @PathVariable UUID goalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<UserAuditLog> auditLogs = auditService.getEntityAuditTrail("GOAL", goalId, page, size);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit trail for user preferences
     * US-094: User Audit Trail
     */
    @GetMapping("/{userId}/preferences/audit")
    public ResponseEntity<List<UserAuditLog>> getPreferencesAuditTrail(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<UserAuditLog> auditLogs = auditService.getEntityAuditTrail("PREFERENCES", userId, page, size);
        return ResponseEntity.ok(auditLogs);
    }
}

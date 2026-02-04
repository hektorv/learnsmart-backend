package com.learnsmart.profile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_audit_log", indexes = {
        @Index(name = "idx_audit_user_timestamp", columnList = "user_id, timestamp DESC"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id, timestamp DESC"),
        @Index(name = "idx_audit_performed_by", columnList = "performed_by, timestamp DESC"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // PROFILE, GOAL, PREFERENCES

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(nullable = false, length = 20)
    private String action; // CREATE, UPDATE, DELETE

    @Column(name = "field_name", length = 100)
    private String fieldName; // For UPDATE actions, specific field changed

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // JSON snapshot of old value

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // JSON snapshot of new value

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IPv4 or IPv6

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = OffsetDateTime.now();
        }
    }
}

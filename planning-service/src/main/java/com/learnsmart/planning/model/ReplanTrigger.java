package com.learnsmart.planning.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "replan_triggers", indexes = {
        @Index(name = "idx_trigger_plan_status", columnList = "plan_id, status"),
        @Index(name = "idx_trigger_status_detected", columnList = "status, detected_at DESC")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "plan")
@EqualsAndHashCode(exclude = "plan")
public class ReplanTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private LearningPlan plan;

    @Column(name = "trigger_type", nullable = false, length = 50)
    private String triggerType; // PROGRESS_DEVIATION, MASTERY_CHANGE, INACTIVITY, GOAL_UPDATED

    @Column(name = "trigger_reason", nullable = false, columnDefinition = "TEXT")
    private String triggerReason;

    @Column(nullable = false, length = 20)
    private String severity; // LOW, MEDIUM, HIGH

    @Column(name = "detected_at", nullable = false)
    private OffsetDateTime detectedAt;

    @Column(name = "evaluated_at")
    private OffsetDateTime evaluatedAt;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, SUGGESTED, DISMISSED, EXECUTED

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON with trigger-specific data

    @PrePersist
    protected void onCreate() {
        if (detectedAt == null) {
            detectedAt = OffsetDateTime.now();
        }
    }
}

package com.learnsmart.planning.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "plan_activities", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "module_id", "position" })
})
@Getter
@Setter
@ToString(exclude = { "module" })
@EqualsAndHashCode(exclude = { "module" })
@NoArgsConstructor
@AllArgsConstructor
public class PlanActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PlanModule module;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "activity_type", nullable = false, length = 30)
    private String activityType;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "content_ref", nullable = false, columnDefinition = "TEXT")
    private String contentRef;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "override_estimated_minutes")
    private Integer overrideEstimatedMinutes;

    // US-110: Activity Completion Timestamps
    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "actual_minutes_spent")
    private Integer actualMinutesSpent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null)
            createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();

        // US-110: Auto-set timestamps based on status changes
        if ("in_progress".equals(status) && startedAt == null) {
            startedAt = OffsetDateTime.now();
        }

        if ("completed".equals(status) && completedAt == null) {
            completedAt = OffsetDateTime.now();

            // Calculate actual time spent if startedAt exists
            if (startedAt != null) {
                long minutes = ChronoUnit.MINUTES.between(startedAt, completedAt);
                actualMinutesSpent = (int) minutes;
            }
        }
    }
}

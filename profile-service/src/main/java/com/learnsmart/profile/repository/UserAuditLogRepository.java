package com.learnsmart.profile.repository;

import com.learnsmart.profile.model.UserAuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, UUID> {

    /**
     * Find audit logs for a specific user, ordered by timestamp descending
     */
    List<UserAuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    /**
     * Find audit logs for a specific entity (e.g., a specific goal)
     */
    List<UserAuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
            String entityType,
            UUID entityId,
            Pageable pageable);

    /**
     * Find audit logs for a user within a date range
     */
    List<UserAuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            UUID userId,
            OffsetDateTime start,
            OffsetDateTime end);

    /**
     * Find audit logs by performer (who made the change)
     */
    List<UserAuditLog> findByPerformedByOrderByTimestampDesc(UUID performedBy, Pageable pageable);

    /**
     * Find audit logs by action type
     */
    List<UserAuditLog> findByUserIdAndActionOrderByTimestampDesc(
            UUID userId,
            String action,
            Pageable pageable);
}

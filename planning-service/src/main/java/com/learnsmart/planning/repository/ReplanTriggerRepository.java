package com.learnsmart.planning.repository;

import com.learnsmart.planning.model.ReplanTrigger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReplanTriggerRepository extends JpaRepository<ReplanTrigger, UUID> {

    /**
     * Find triggers for a specific plan with a given status
     */
    List<ReplanTrigger> findByPlanIdAndStatus(UUID planId, String status);

    /**
     * Find all triggers with a specific status, ordered by detection time
     */
    List<ReplanTrigger> findByStatusOrderByDetectedAtDesc(String status, Pageable pageable);

    /**
     * Find the most recent trigger of a specific type for a plan
     */
    Optional<ReplanTrigger> findTopByPlanIdAndTriggerTypeOrderByDetectedAtDesc(
            UUID planId,
            String triggerType);

    /**
     * Find all pending triggers for a plan
     */
    List<ReplanTrigger> findByPlanIdAndStatusOrderByDetectedAtDesc(UUID planId, String status);
}

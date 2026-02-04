package com.learnsmart.profile.repository;

import com.learnsmart.profile.model.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserGoalRepository extends JpaRepository<UserGoal, UUID> {

    List<UserGoal> findByUserId(UUID userId);

    // US-096: Find goals by status
    List<UserGoal> findByUserIdAndStatus(UUID userId, String status);

    // US-096: Find completed goals ordered by completion date
    @Query("SELECT g FROM UserGoal g WHERE g.userId = :userId AND g.status = 'completed' ORDER BY g.completedAt DESC")
    List<UserGoal> findCompletedByUserId(@Param("userId") UUID userId);

    // US-096: Find in-progress goals ordered by progress percentage
    @Query("SELECT g FROM UserGoal g WHERE g.userId = :userId AND g.status = 'in_progress' ORDER BY g.completionPercentage DESC")
    List<UserGoal> findInProgressByUserId(@Param("userId") UUID userId);
}

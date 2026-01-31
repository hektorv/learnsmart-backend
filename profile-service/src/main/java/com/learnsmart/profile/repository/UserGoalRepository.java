package com.learnsmart.profile.repository;

import com.learnsmart.profile.model.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UserGoalRepository extends JpaRepository<UserGoal, UUID> {
    List<UserGoal> findByUserId(UUID userId);
}

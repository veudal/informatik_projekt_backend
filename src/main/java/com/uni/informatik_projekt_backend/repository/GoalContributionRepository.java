package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalContributionRepository extends JpaRepository<GoalContribution, UUID> {
    List<GoalContribution> findByGoalId(UUID goalId);
    List<GoalContribution> findByUserId(UUID userId);
}

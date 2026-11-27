package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.GoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GoalProgressRepository extends JpaRepository<GoalProgress, UUID> {
    List<GoalProgress> findByFamilyId(UUID familyId);
}
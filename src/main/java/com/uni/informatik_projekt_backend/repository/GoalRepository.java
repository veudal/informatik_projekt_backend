package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByFamilyId(UUID familyId);
    List<Goal> findByOwnerId(UUID ownerId);
    List<Goal> findByFamilyIdAndStatus(UUID familyId, String status);
}

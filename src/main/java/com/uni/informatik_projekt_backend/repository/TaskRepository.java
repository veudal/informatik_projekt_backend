package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByFamilyId(UUID familyId);
    List<Task> findByAssignedTo(UUID assignedTo);
    List<Task> findByCreatedBy(UUID createdBy);
    List<Task> findByFamilyIdAndStatus(UUID familyId, String status);
}

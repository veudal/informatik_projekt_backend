package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, UUID> {

    List<Chore> findByFamilyId(UUID familyId);

    List<Chore> findByAssignedTo(UUID assignedTo);
}
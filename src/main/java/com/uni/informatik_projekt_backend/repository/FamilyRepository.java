package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FamilyRepository extends JpaRepository<Family, UUID> {

    @Query("SELECT f FROM Family f WHERE f.id = (SELECT u.familyId FROM User u WHERE u.id = :userId)")
    Optional<Family> findByUserId(@Param("userId") UUID userId);
}
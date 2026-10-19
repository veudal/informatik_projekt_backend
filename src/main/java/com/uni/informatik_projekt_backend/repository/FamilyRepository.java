package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FamilyRepository extends JpaRepository<Family, UUID> {
    // Spring Data JPA automatically handles basic CRUD operations (like .findAll())
}
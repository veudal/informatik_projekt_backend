package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.RecurringTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecurringTransferRepository extends JpaRepository<RecurringTransfer, UUID> {
    List<RecurringTransfer> findByFamilyId(UUID familyId);
    List<RecurringTransfer> findByActiveTrue();
}

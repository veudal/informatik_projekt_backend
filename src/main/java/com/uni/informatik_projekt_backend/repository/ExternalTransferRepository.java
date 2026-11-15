package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.ExternalTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExternalTransferRepository extends JpaRepository<ExternalTransfer, UUID> {
    List<ExternalTransfer> findByFamilyWalletId(UUID familyWalletId);
}

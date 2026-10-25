package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.FamilyWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface FamilyWalletRepository extends JpaRepository<FamilyWallet, UUID> {
    List<FamilyWallet> findByFamilyId(UUID familyId);
}

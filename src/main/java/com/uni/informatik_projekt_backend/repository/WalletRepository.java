package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findByFamilyId(UUID familyId);

    List<Wallet> findByOwnerId(UUID ownerId);

    @Query(value = "SELECT COALESCE(SUM(amount_cents), 0) FROM transactions WHERE wallet_id = ?1", nativeQuery = true)
    Long getBalanceByWalletId(UUID walletId);
}
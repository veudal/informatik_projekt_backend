package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByWalletId(UUID walletId);

    List<Transaction> findByFamilyId(UUID familyId);
}
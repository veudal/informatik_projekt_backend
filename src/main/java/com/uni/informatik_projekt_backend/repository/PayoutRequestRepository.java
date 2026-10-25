package com.uni.informatik_projekt_backend.repository;

import com.uni.informatik_projekt_backend.model.PayoutRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayoutRequestRepository extends JpaRepository<PayoutRequest, UUID> {
    List<PayoutRequest> findByFamilyWalletId(UUID familyWalletId);
    List<PayoutRequest> findByRequestedBy(UUID requestedBy);
    List<PayoutRequest> findByStatus(String status);
}

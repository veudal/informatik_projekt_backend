package com.uni.informatik_projekt_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payout_requests")
public class PayoutRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "family_wallet_id", nullable = false)
    private UUID familyWalletId;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(nullable = false)
    private String method; // 'transfer' or 'cash'

    @Column(nullable = false)
    private String status; // 'requested', 'approved', 'paid', 'rejected'

    @Column(name = "decided_by")
    private UUID decidedBy;

    @Column(name = "tx_id")
    private UUID txId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PayoutRequest() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFamilyWalletId() {
        return familyWalletId;
    }

    public void setFamilyWalletId(UUID familyWalletId) {
        this.familyWalletId = familyWalletId;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public UUID getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UUID requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(Long amountCents) {
        this.amountCents = amountCents;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(UUID decidedBy) {
        this.decidedBy = decidedBy;
    }

    public UUID getTxId() {
        return txId;
    }

    public void setTxId(UUID txId) {
        this.txId = txId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

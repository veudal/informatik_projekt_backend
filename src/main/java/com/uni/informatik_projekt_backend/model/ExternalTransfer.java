package com.uni.informatik_projekt_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * Simulierte Ein- bzw. Auszahlung zwischen einem Familienkonto und einer externen Bank (z. B. Sparkasse).
 * amountCents ist aus Sicht des Familienkontos vorzeichenbehaftet: positiv = Einzahlung, negativ = Auszahlung.
 */
@Entity
@Table(name = "external_transfers")
public class ExternalTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "family_wallet_id", nullable = false)
    private UUID familyWalletId;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ExternalTransfer() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getFamilyWalletId() { return familyWalletId; }
    public void setFamilyWalletId(UUID familyWalletId) { this.familyWalletId = familyWalletId; }

    public UUID getFamilyId() { return familyId; }
    public void setFamilyId(UUID familyId) { this.familyId = familyId; }

    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
}

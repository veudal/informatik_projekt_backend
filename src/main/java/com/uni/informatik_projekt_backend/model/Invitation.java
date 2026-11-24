package com.uni.informatik_projekt_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invitations")
public class Invitation {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "issuer_id", nullable = false)
    private UUID issuerId;

    @Column(name = "redeemer_id")
    private UUID redeemerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvitationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public UUID getIssuerId() {
        return issuerId;
    }

    public UUID getRedeemerId() {
        return redeemerId;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIssuerId(UUID issuerId) {
        this.issuerId = issuerId;
    }

    public void setRedeemerId(UUID redeemerId) {
        this.redeemerId = redeemerId;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }
}

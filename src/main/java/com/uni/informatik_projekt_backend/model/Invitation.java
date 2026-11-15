package com.uni.informatik_projekt_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invitations")
public class Invitation {
    public static final String REDEEM_STATUS_REDEEMABLE = "REDEEMABLE";
    public static final String REDEEM_STATUS_PENDING = "PENDING";
    public static final String REDEEM_STATUS_ACCEPTED = "ACCEPTED";
    public static final String REDEEM_STATUS_DENIED = "DENIED";

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "issuer_id", nullable = false)
    private UUID issuerId;

    @Column(name = "redeemer_id")
    private UUID redeemerId;

    @Column(name = "redeem_status")
    private String redeemStatus;

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

    public String getRedeemStatus() {
        return redeemStatus;
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

    public void setRedeemStatus(String redeemStatus) {
        this.redeemStatus = redeemStatus;
    }
}

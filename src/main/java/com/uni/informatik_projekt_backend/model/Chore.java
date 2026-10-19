package com.uni.informatik_projekt_backend.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "chores")
public class Chore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "assigned_to", nullable = false)
    private UUID assignedTo;

    @Column(nullable = false)
    private String title;

    @Column(name = "reward_cents", columnDefinition = "BIGINT DEFAULT 0")
    private Long rewardCents;

    @Column(nullable = false, columnDefinition = "TEXT DEFAULT 'open'")
    private String status; // 'open', 'done', 'approved'

    @Column(name = "created_at")
    private Date createdAt;

    public Chore() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getRewardCents() {
        return rewardCents;
    }

    public void setRewardCents(Long rewardCents) {
        this.rewardCents = rewardCents;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
package com.uni.informatik_projekt_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(nullable = false)
    private String title;

    @Column(name = "reward_cents", nullable = false)
    private Long rewardCents;

    @Column(name = "due_date")
    private LocalDate dueDate;

    private String recurrence; // 'daily', 'weekly', 'monthly'

    @Column(nullable = false)
    private String status; // 'open', 'done', 'approved', 'rejected'

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "reward_tx_id")
    private UUID rewardTxId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Task() {}

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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    public UUID getRewardTxId() {
        return rewardTxId;
    }

    public void setRewardTxId(UUID rewardTxId) {
        this.rewardTxId = rewardTxId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

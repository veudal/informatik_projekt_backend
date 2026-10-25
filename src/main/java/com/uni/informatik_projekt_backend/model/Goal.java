package com.uni.informatik_projekt_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "goal_name", nullable = false)
    private String goalName;

    @Column(name = "target_cents", nullable = false)
    private Long targetCents;

    @Column(nullable = false)
    private String status; // 'active', 'reached', 'cancelled'

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "current_cents")
    private Long currentCents;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Goal() {}

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

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public Long getTargetCents() {
        return targetCents;
    }

    public void setTargetCents(Long targetCents) {
        this.targetCents = targetCents;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Long getCurrentCents() {
        return currentCents;
    }

    public void setCurrentCents(Long currentCents) {
        this.currentCents = currentCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

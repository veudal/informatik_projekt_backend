package com.uni.informatik_projekt_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "goal_progress")
public class GoalProgress {
    @Id
    @Column(name = "goal_id")
    private UUID goalId;
    @Column(name = "family_id")
    private UUID familyId;
    @Column(name = "owner_id")
    private UUID ownerId;
    @Column(name = "goal_name")
    private String goalName;
    @Column(name = "target_cents")
    private Long targetCents;
    @Column(name = "status")
    private String status;
    @Column(name = "deadline")
    private LocalDate deadline;
    @Column(name = "reached_at")
    private Instant reachedAt;
    @Column(name = "saved_cents")
    private Long savedCents;
    @Column(name = "percent")
    private BigDecimal percent;

    public UUID getGoalId() {
        return goalId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getGoalName() {
        return goalName;
    }

    public Long getTargetCents() {
        return targetCents;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Instant getReachedAt() {
        return reachedAt;
    }

    public Long getSavedCents() {
        return savedCents;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "goal_participants", joinColumns = @JoinColumn(name = "goal_id"))
    @Column(name = "user_id")
    private Set<UUID> participantIds = new HashSet<>();

    public Set<UUID> getParticipantIds() {
        return participantIds;
    }

    // aliases so the old JSON shape still works
    public UUID getId() {
        return goalId;
    }

    public Long getCurrentCents() {
        return savedCents;
    }
}
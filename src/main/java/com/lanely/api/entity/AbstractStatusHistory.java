package com.lanely.api.entity;

import com.lanely.api.entity.enums.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public abstract class AbstractStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_account_id")
    private Account changedBy;

    @Column(name = "changed_by_name", nullable = false, length = 150)
    private String changedByName;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 16)
    private AccountType actorType;

    @Column(columnDefinition = "text")
    private String note;

    private Double latitude;

    private Double longitude;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public Account getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Account changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public AccountType getActorType() {
        return actorType;
    }

    public void setActorType(AccountType actorType) {
        this.actorType = actorType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }
}

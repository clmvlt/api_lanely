package com.lanely.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "vehicle_mileage_readings",
        indexes = @Index(name = "idx_mileage_vehicle_recorded", columnList = "vehicle_id, recorded_at"))
public class VehicleMileageReading extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "value_km", nullable = false)
    private int valueKm;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private Account recordedBy;

    @Column(columnDefinition = "text")
    private String note;

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public int getValueKm() {
        return valueKm;
    }

    public void setValueKm(int valueKm) {
        this.valueKm = valueKm;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Account getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(Account recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

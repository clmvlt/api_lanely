package com.lanely.api.entity;

import com.lanely.api.entity.enums.ParcelStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "waybill_goods_lines", indexes = @Index(name = "idx_waybill_goods_waybill", columnList = "waybill_id"))
public class WaybillGoodsLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waybill_id", nullable = false)
    private Waybill waybill;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @ColumnDefault("'PENDING'")
    private ParcelStatus status = ParcelStatus.PENDING;

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt asc")
    private List<ParcelStatusHistory> statusHistory = new ArrayList<>();

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "packaging_type", length = 64)
    private String packagingType;

    @Column(name = "number_of_packages")
    private Integer numberOfPackages;

    @Column(name = "marks_and_numbers", length = 255)
    private String marksAndNumbers;

    @Column(name = "gross_weight_kg", precision = 12, scale = 3)
    private BigDecimal grossWeightKg;

    @Column(name = "volume_m3", precision = 12, scale = 3)
    private BigDecimal volumeM3;

    @Column(name = "length_cm", precision = 12, scale = 2)
    private BigDecimal lengthCm;

    @Column(name = "width_cm", precision = 12, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", precision = 12, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "dangerous_goods", nullable = false)
    private boolean dangerousGoods = false;

    @Column(name = "un_number", length = 8)
    private String unNumber;

    @Column(name = "dock_entered_at")
    private Instant dockEnteredAt;

    @Column(name = "dock_exited_at")
    private Instant dockExitedAt;

    public Waybill getWaybill() {
        return waybill;
    }

    public void setWaybill(Waybill waybill) {
        this.waybill = waybill;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public void setStatus(ParcelStatus status) {
        this.status = status;
    }

    public List<ParcelStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }

    public Integer getNumberOfPackages() {
        return numberOfPackages;
    }

    public void setNumberOfPackages(Integer numberOfPackages) {
        this.numberOfPackages = numberOfPackages;
    }

    public String getMarksAndNumbers() {
        return marksAndNumbers;
    }

    public void setMarksAndNumbers(String marksAndNumbers) {
        this.marksAndNumbers = marksAndNumbers;
    }

    public BigDecimal getGrossWeightKg() {
        return grossWeightKg;
    }

    public void setGrossWeightKg(BigDecimal grossWeightKg) {
        this.grossWeightKg = grossWeightKg;
    }

    public BigDecimal getVolumeM3() {
        return volumeM3;
    }

    public void setVolumeM3(BigDecimal volumeM3) {
        this.volumeM3 = volumeM3;
    }

    public BigDecimal getLengthCm() {
        return lengthCm;
    }

    public void setLengthCm(BigDecimal lengthCm) {
        this.lengthCm = lengthCm;
    }

    public BigDecimal getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(BigDecimal widthCm) {
        this.widthCm = widthCm;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public boolean isDangerousGoods() {
        return dangerousGoods;
    }

    public void setDangerousGoods(boolean dangerousGoods) {
        this.dangerousGoods = dangerousGoods;
    }

    public String getUnNumber() {
        return unNumber;
    }

    public void setUnNumber(String unNumber) {
        this.unNumber = unNumber;
    }

    public Instant getDockEnteredAt() {
        return dockEnteredAt;
    }

    public void setDockEnteredAt(Instant dockEnteredAt) {
        this.dockEnteredAt = dockEnteredAt;
    }

    public Instant getDockExitedAt() {
        return dockExitedAt;
    }

    public void setDockExitedAt(Instant dockExitedAt) {
        this.dockExitedAt = dockExitedAt;
    }
}

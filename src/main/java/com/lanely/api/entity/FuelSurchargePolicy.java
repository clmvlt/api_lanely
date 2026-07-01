package com.lanely.api.entity;

import com.lanely.api.entity.enums.FuelSurchargeMode;
import com.lanely.api.entity.enums.FuelType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fuel_surcharge_policies",
        uniqueConstraints = @UniqueConstraint(name = "uk_fuel_policy_tariff", columnNames = "tariff_id"))
public class FuelSurchargePolicy extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(nullable = false)
    private boolean enabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 16)
    private FuelType fuelType = FuelType.DIESEL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private FuelSurchargeMode mode = FuelSurchargeMode.THRESHOLD_COMPONENTS;

    @Column(name = "threshold_price", precision = 8, scale = 4)
    private BigDecimal thresholdPrice;

    @Column(name = "reference_price", precision = 8, scale = 4)
    private BigDecimal referencePrice;

    @Column(name = "diesel_share_ratio", precision = 5, scale = 4)
    private BigDecimal dieselShareRatio;

    @Column(name = "clamp_at_zero", nullable = false)
    private boolean clampAtZero = true;

    @Column(name = "source_filter", length = 64)
    private String sourceFilter;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position asc")
    private List<FuelSurchargeComponent> surchargeComponents = new ArrayList<>();

    public Tariff getTariff() {
        return tariff;
    }

    public void setTariff(Tariff tariff) {
        this.tariff = tariff;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public FuelSurchargeMode getMode() {
        return mode;
    }

    public void setMode(FuelSurchargeMode mode) {
        this.mode = mode;
    }

    public BigDecimal getThresholdPrice() {
        return thresholdPrice;
    }

    public void setThresholdPrice(BigDecimal thresholdPrice) {
        this.thresholdPrice = thresholdPrice;
    }

    public BigDecimal getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(BigDecimal referencePrice) {
        this.referencePrice = referencePrice;
    }

    public BigDecimal getDieselShareRatio() {
        return dieselShareRatio;
    }

    public void setDieselShareRatio(BigDecimal dieselShareRatio) {
        this.dieselShareRatio = dieselShareRatio;
    }

    public boolean isClampAtZero() {
        return clampAtZero;
    }

    public void setClampAtZero(boolean clampAtZero) {
        this.clampAtZero = clampAtZero;
    }

    public String getSourceFilter() {
        return sourceFilter;
    }

    public void setSourceFilter(String sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    public List<FuelSurchargeComponent> getSurchargeComponents() {
        return surchargeComponents;
    }
}

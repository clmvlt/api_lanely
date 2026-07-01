package com.lanely.api.entity;

import com.lanely.api.entity.enums.TariffStatus;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tariffs", indexes = {
        @Index(name = "idx_tariff_company_status", columnList = "company_id, status"),
        @Index(name = "idx_tariff_company_client", columnList = "company_id, client_id")
})
public class Tariff extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TariffStatus status = TariffStatus.DRAFT;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "rounding_mode", nullable = false, length = 16)
    private RoundingMode roundingMode = RoundingMode.HALF_UP;

    @Column(name = "rounding_scale", nullable = false)
    private int roundingScale = 2;

    @Column(name = "min_charge_amount", precision = 12, scale = 2)
    private BigDecimal minChargeAmount;

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position asc")
    private List<TariffComponent> components = new ArrayList<>();

    @OneToOne(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FuelSurchargePolicy fuelSurcharge;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public TariffStatus getStatus() {
        return status;
    }

    public void setStatus(TariffStatus status) {
        this.status = status;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    public void setRoundingMode(RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }

    public int getRoundingScale() {
        return roundingScale;
    }

    public void setRoundingScale(int roundingScale) {
        this.roundingScale = roundingScale;
    }

    public BigDecimal getMinChargeAmount() {
        return minChargeAmount;
    }

    public void setMinChargeAmount(BigDecimal minChargeAmount) {
        this.minChargeAmount = minChargeAmount;
    }

    public List<TariffComponent> getComponents() {
        return components;
    }

    public FuelSurchargePolicy getFuelSurcharge() {
        return fuelSurcharge;
    }

    public void setFuelSurcharge(FuelSurchargePolicy fuelSurcharge) {
        this.fuelSurcharge = fuelSurcharge;
    }
}

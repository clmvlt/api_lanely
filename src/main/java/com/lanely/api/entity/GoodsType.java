package com.lanely.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "goods_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_goods_type_company_name", columnNames = {"company_id", "name"}),
        indexes = @Index(name = "idx_goods_type_company_name", columnList = "company_id, name"))
public class GoodsType extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "packaging_type", length = 64)
    private String packagingType;

    @Column(name = "number_of_packages")
    private Integer numberOfPackages;

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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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
}

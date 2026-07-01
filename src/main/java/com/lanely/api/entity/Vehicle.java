package com.lanely.api.entity;

import com.lanely.api.entity.embeddable.InsuranceInfo;
import com.lanely.api.entity.enums.FuelType;
import com.lanely.api.entity.enums.VehicleStatus;
import com.lanely.api.entity.enums.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_company_plate", columnNames = {"company_id", "registration_plate"}),
        indexes = {
                @Index(name = "idx_vehicle_company_status", columnList = "company_id, status"),
                @Index(name = "idx_vehicle_company_type", columnList = "company_id, vehicle_type")
        })
public class Vehicle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "registration_plate", nullable = false, length = 16)
    private String registrationPlate;

    @Column(length = 17)
    private String vin;

    @Column(length = 64)
    private String make;

    @Column(length = 64)
    private String model;

    @Column(length = 128)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 16)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 16)
    private FuelType fuelType;

    @Column(name = "first_registration_date")
    private LocalDate firstRegistrationDate;

    @Column(name = "emission_class", length = 32)
    private String emissionClass;

    @Column(name = "gross_weight_kg")
    private Integer grossWeightKg;

    @Column(name = "payload_kg")
    private Integer payloadKg;

    @Column(name = "registration_certificate_number", length = 64)
    private String registrationCertificateNumber;

    @Embedded
    private InsuranceInfo insuranceInfo = new InsuranceInfo();

    @Column(name = "technical_inspection_date")
    private LocalDate technicalInspectionDate;

    @Column(name = "road_tax_due_date")
    private LocalDate roadTaxDueDate;

    @Column(name = "latest_mileage_km")
    private Integer latestMileageKm;

    @Column(name = "latest_mileage_at")
    private Instant latestMileageAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(columnDefinition = "text")
    private String notes;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getRegistrationPlate() {
        return registrationPlate;
    }

    public void setRegistrationPlate(String registrationPlate) {
        this.registrationPlate = registrationPlate;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public LocalDate getFirstRegistrationDate() {
        return firstRegistrationDate;
    }

    public void setFirstRegistrationDate(LocalDate firstRegistrationDate) {
        this.firstRegistrationDate = firstRegistrationDate;
    }

    public String getEmissionClass() {
        return emissionClass;
    }

    public void setEmissionClass(String emissionClass) {
        this.emissionClass = emissionClass;
    }

    public Integer getGrossWeightKg() {
        return grossWeightKg;
    }

    public void setGrossWeightKg(Integer grossWeightKg) {
        this.grossWeightKg = grossWeightKg;
    }

    public Integer getPayloadKg() {
        return payloadKg;
    }

    public void setPayloadKg(Integer payloadKg) {
        this.payloadKg = payloadKg;
    }

    public String getRegistrationCertificateNumber() {
        return registrationCertificateNumber;
    }

    public void setRegistrationCertificateNumber(String registrationCertificateNumber) {
        this.registrationCertificateNumber = registrationCertificateNumber;
    }

    public InsuranceInfo getInsuranceInfo() {
        return insuranceInfo;
    }

    public void setInsuranceInfo(InsuranceInfo insuranceInfo) {
        this.insuranceInfo = insuranceInfo;
    }

    public LocalDate getTechnicalInspectionDate() {
        return technicalInspectionDate;
    }

    public void setTechnicalInspectionDate(LocalDate technicalInspectionDate) {
        this.technicalInspectionDate = technicalInspectionDate;
    }

    public LocalDate getRoadTaxDueDate() {
        return roadTaxDueDate;
    }

    public void setRoadTaxDueDate(LocalDate roadTaxDueDate) {
        this.roadTaxDueDate = roadTaxDueDate;
    }

    public Integer getLatestMileageKm() {
        return latestMileageKm;
    }

    public void setLatestMileageKm(Integer latestMileageKm) {
        this.latestMileageKm = latestMileageKm;
    }

    public Instant getLatestMileageAt() {
        return latestMileageAt;
    }

    public void setLatestMileageAt(Instant latestMileageAt) {
        this.latestMileageAt = latestMileageAt;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

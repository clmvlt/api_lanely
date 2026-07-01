package com.lanely.api.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public class InsuranceInfo {

    @Column(name = "insurance_insurer_name", length = 200)
    private String insurerName;

    @Column(name = "insurance_policy_number", length = 64)
    private String policyNumber;

    @Column(name = "insurance_coverage_type", length = 64)
    private String coverageType;

    @Column(name = "insurance_start_date")
    private LocalDate startDate;

    @Column(name = "insurance_end_date")
    private LocalDate endDate;

    @Column(name = "insurance_contact", length = 200)
    private String contact;

    public String getInsurerName() {
        return insurerName;
    }

    public void setInsurerName(String insurerName) {
        this.insurerName = insurerName;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(String coverageType) {
        this.coverageType = coverageType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}

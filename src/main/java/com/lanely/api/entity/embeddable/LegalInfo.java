package com.lanely.api.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LegalInfo {

    @Column(name = "legal_name", length = 200)
    private String legalName;

    @Column(name = "legal_registration_number", length = 64)
    private String registrationNumber;

    @Column(name = "legal_vat_number", length = 32)
    private String vatNumber;

    @Column(name = "legal_form", length = 64)
    private String legalForm;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }
}

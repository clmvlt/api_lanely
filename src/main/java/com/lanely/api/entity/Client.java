package com.lanely.api.entity;

import com.lanely.api.entity.embeddable.ClientSettings;
import com.lanely.api.entity.embeddable.LegalInfo;
import com.lanely.api.entity.enums.ClientStatus;
import com.lanely.api.entity.enums.ClientType;
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

@Entity
@Table(name = "clients",
        uniqueConstraints = @UniqueConstraint(name = "uk_client_company_reference", columnNames = {"company_id", "reference"}),
        indexes = @Index(name = "idx_client_company_status", columnList = "company_id, status"))
public class Client extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 32)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ClientType type;

    @Column(nullable = false, length = 200)
    private String name;

    @Embedded
    private LegalInfo legalInfo = new LegalInfo();

    @Column(length = 255)
    private String email;

    @Column(length = 32)
    private String phone;

    @Column(length = 255)
    private String website;

    @Column(name = "payment_terms_days", nullable = false)
    private int paymentTermsDays = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ClientStatus status = ClientStatus.ACTIVE;

    @Column(name = "delivery_blocked", nullable = false)
    private boolean deliveryBlocked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_manager_id")
    private CompanyMember accountManager;

    @Column(columnDefinition = "text")
    private String notes;

    @Embedded
    private ClientSettings settings = new ClientSettings();

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ClientType getType() {
        return type;
    }

    public void setType(ClientType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LegalInfo getLegalInfo() {
        return legalInfo;
    }

    public void setLegalInfo(LegalInfo legalInfo) {
        this.legalInfo = legalInfo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getPaymentTermsDays() {
        return paymentTermsDays;
    }

    public void setPaymentTermsDays(int paymentTermsDays) {
        this.paymentTermsDays = paymentTermsDays;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public boolean isDeliveryBlocked() {
        return deliveryBlocked;
    }

    public void setDeliveryBlocked(boolean deliveryBlocked) {
        this.deliveryBlocked = deliveryBlocked;
    }

    public CompanyMember getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(CompanyMember accountManager) {
        this.accountManager = accountManager;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ClientSettings getSettings() {
        return settings;
    }

    public void setSettings(ClientSettings settings) {
        this.settings = settings;
    }
}

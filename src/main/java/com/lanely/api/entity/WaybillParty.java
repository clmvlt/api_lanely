package com.lanely.api.entity;

import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.embeddable.LegalInfo;
import com.lanely.api.entity.enums.WaybillPartyRole;
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

import java.util.UUID;

@Entity
@Table(name = "waybill_parties", indexes = @Index(name = "idx_waybill_party_waybill", columnList = "waybill_id"))
public class WaybillParty extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waybill_id", nullable = false)
    private Waybill waybill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WaybillPartyRole role;

    @Column(nullable = false, length = 200)
    private String name;

    @Embedded
    private Address address = new Address();

    @Embedded
    private GeoPoint location = new GeoPoint();

    @Column(name = "contact_name", length = 150)
    private String contactName;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Embedded
    private LegalInfo legalInfo = new LegalInfo();

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "client_address_id")
    private UUID clientAddressId;

    public Waybill getWaybill() {
        return waybill;
    }

    public void setWaybill(Waybill waybill) {
        this.waybill = waybill;
    }

    public WaybillPartyRole getRole() {
        return role;
    }

    public void setRole(WaybillPartyRole role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public LegalInfo getLegalInfo() {
        return legalInfo;
    }

    public void setLegalInfo(LegalInfo legalInfo) {
        this.legalInfo = legalInfo;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getClientAddressId() {
        return clientAddressId;
    }

    public void setClientAddressId(UUID clientAddressId) {
        this.clientAddressId = clientAddressId;
    }
}

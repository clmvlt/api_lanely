package com.lanely.api.entity;

import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.embeddable.LegalInfo;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 16)
    private String publicCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    @Embedded
    private LegalInfo legalInfo = new LegalInfo();

    @Embedded
    private Address billingAddress = new Address();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line1", column = @Column(name = "deposit_address_line1", length = 200)),
            @AttributeOverride(name = "line2", column = @Column(name = "deposit_address_line2", length = 200)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "deposit_address_postal_code", length = 20)),
            @AttributeOverride(name = "city", column = @Column(name = "deposit_address_city", length = 120)),
            @AttributeOverride(name = "state", column = @Column(name = "deposit_address_state", length = 120)),
            @AttributeOverride(name = "country", column = @Column(name = "deposit_address_country", length = 2))
    })
    private Address depositAddress = new Address();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "deposit_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "deposit_longitude"))
    })
    private GeoPoint depositCoordinate = new GeoPoint();

    @Column(name = "billing_email", length = 255)
    private String billingEmail;

    @Column(name = "billing_phone", length = 32)
    private String billingPhone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Image getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }

    public LegalInfo getLegalInfo() {
        return legalInfo;
    }

    public void setLegalInfo(LegalInfo legalInfo) {
        this.legalInfo = legalInfo;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public Address getDepositAddress() {
        return depositAddress;
    }

    public void setDepositAddress(Address depositAddress) {
        this.depositAddress = depositAddress;
    }

    public GeoPoint getDepositCoordinate() {
        return depositCoordinate;
    }

    public void setDepositCoordinate(GeoPoint depositCoordinate) {
        this.depositCoordinate = depositCoordinate;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }

    public String getBillingPhone() {
        return billingPhone;
    }

    public void setBillingPhone(String billingPhone) {
        this.billingPhone = billingPhone;
    }
}

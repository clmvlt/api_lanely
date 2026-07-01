package com.lanely.api.entity;

import com.lanely.api.entity.enums.SignatureMethod;
import com.lanely.api.entity.enums.WaybillPartyRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "waybill_signatures", indexes = @Index(name = "idx_waybill_signature_waybill", columnList = "waybill_id"))
public class WaybillSignature extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waybill_id", nullable = false)
    private Waybill waybill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WaybillPartyRole role;

    @Column(name = "signer_name", nullable = false, length = 150)
    private String signerName;

    @Column(name = "signed_at", nullable = false)
    private Instant signedAt;

    @Column(length = 200)
    private String place;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SignatureMethod method = SignatureMethod.CLICKWRAP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_image_id")
    private Image signatureImage;

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

    public String getSignerName() {
        return signerName;
    }

    public void setSignerName(String signerName) {
        this.signerName = signerName;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Instant signedAt) {
        this.signedAt = signedAt;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public SignatureMethod getMethod() {
        return method;
    }

    public void setMethod(SignatureMethod method) {
        this.method = method;
    }

    public Image getSignatureImage() {
        return signatureImage;
    }

    public void setSignatureImage(Image signatureImage) {
        this.signatureImage = signatureImage;
    }
}

package com.lanely.api.entity;

import com.lanely.api.entity.enums.ParcelStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "parcel_status_history",
        indexes = @Index(name = "idx_parcel_status_history_parcel", columnList = "parcel_id"))
public class ParcelStatusHistory extends AbstractStatusHistory {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcel_id", nullable = false)
    private WaybillGoodsLine parcel;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 16)
    private ParcelStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 16)
    private ParcelStatus toStatus;

    public WaybillGoodsLine getParcel() {
        return parcel;
    }

    public void setParcel(WaybillGoodsLine parcel) {
        this.parcel = parcel;
    }

    public ParcelStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ParcelStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ParcelStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ParcelStatus toStatus) {
        this.toStatus = toStatus;
    }
}

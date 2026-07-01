package com.lanely.api.entity;

import com.lanely.api.entity.enums.WaybillStatus;
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
@Table(name = "waybill_status_history",
        indexes = @Index(name = "idx_waybill_status_history_waybill", columnList = "waybill_id"))
public class WaybillStatusHistory extends AbstractStatusHistory {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waybill_id", nullable = false)
    private Waybill waybill;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 16)
    private WaybillStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 16)
    private WaybillStatus toStatus;

    public Waybill getWaybill() {
        return waybill;
    }

    public void setWaybill(Waybill waybill) {
        this.waybill = waybill;
    }

    public WaybillStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(WaybillStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public WaybillStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(WaybillStatus toStatus) {
        this.toStatus = toStatus;
    }
}

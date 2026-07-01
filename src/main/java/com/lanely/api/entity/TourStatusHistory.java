package com.lanely.api.entity;

import com.lanely.api.entity.enums.TourStatus;
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
@Table(name = "tour_status_history",
        indexes = @Index(name = "idx_tour_status_history_tour", columnList = "tour_id"))
public class TourStatusHistory extends AbstractStatusHistory {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 16)
    private TourStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 16)
    private TourStatus toStatus;

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public TourStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(TourStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public TourStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(TourStatus toStatus) {
        this.toStatus = toStatus;
    }
}

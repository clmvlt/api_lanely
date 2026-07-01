package com.lanely.api.entity;

import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.embeddable.RouteInfo;
import com.lanely.api.entity.enums.TourStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours",
        uniqueConstraints = @UniqueConstraint(name = "uk_tour_company_reference", columnNames = {"company_id", "reference"}),
        indexes = {
                @Index(name = "idx_tour_company_status", columnList = "company_id, status"),
                @Index(name = "idx_tour_assigned_account", columnList = "assigned_account_id")
        })
public class Tour extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 32)
    private String reference;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TourStatus status = TourStatus.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_account_id")
    private Account assignedAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Embedded
    private Address depot = new Address();

    @Embedded
    private GeoPoint depotLocation = new GeoPoint();

    @Column(name = "planned_date")
    private LocalDate plannedDate;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Embedded
    private RouteInfo route = new RouteInfo();

    @Column(name = "last_optimized_at")
    private Instant lastOptimizedAt;

    @Column(columnDefinition = "text")
    private String notes;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt asc")
    private List<TourStatusHistory> statusHistory = new ArrayList<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TourStatus getStatus() {
        return status;
    }

    public void setStatus(TourStatus status) {
        this.status = status;
    }

    public Account getAssignedAccount() {
        return assignedAccount;
    }

    public void setAssignedAccount(Account assignedAccount) {
        this.assignedAccount = assignedAccount;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Address getDepot() {
        return depot;
    }

    public void setDepot(Address depot) {
        this.depot = depot;
    }

    public GeoPoint getDepotLocation() {
        return depotLocation;
    }

    public void setDepotLocation(GeoPoint depotLocation) {
        this.depotLocation = depotLocation;
    }

    public LocalDate getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(LocalDate plannedDate) {
        this.plannedDate = plannedDate;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public RouteInfo getRoute() {
        return route;
    }

    public void setRoute(RouteInfo route) {
        this.route = route;
    }

    public Instant getLastOptimizedAt() {
        return lastOptimizedAt;
    }

    public void setLastOptimizedAt(Instant lastOptimizedAt) {
        this.lastOptimizedAt = lastOptimizedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<TourStatusHistory> getStatusHistory() {
        return statusHistory;
    }
}

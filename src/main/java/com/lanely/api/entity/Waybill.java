package com.lanely.api.entity;

import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.embeddable.RouteInfo;
import com.lanely.api.entity.enums.WaybillPartyRole;
import com.lanely.api.entity.enums.WaybillScope;
import com.lanely.api.entity.enums.WaybillStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "waybills",
        uniqueConstraints = @UniqueConstraint(name = "uk_waybill_company_reference", columnNames = {"company_id", "reference"}),
        indexes = {
                @Index(name = "idx_waybill_company_status", columnList = "company_id, status"),
                @Index(name = "idx_waybill_company_archived", columnList = "company_id, archived"),
                @Index(name = "idx_waybill_client", columnList = "client_id"),
                @Index(name = "idx_waybill_tour", columnList = "tour_id"),
                @Index(name = "idx_waybill_assigned_account", columnList = "assigned_account_id"),
                @Index(name = "idx_waybill_company_dock", columnList = "company_id, dock_entered_at")
        })
public class Waybill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false, length = 32)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WaybillStatus status = WaybillStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WaybillScope scope = WaybillScope.NATIONAL;

    @OneToMany(mappedBy = "waybill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaybillParty> parties = new ArrayList<>();

    @OneToMany(mappedBy = "waybill", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position asc")
    private List<WaybillGoodsLine> goodsLines = new ArrayList<>();

    @OneToMany(mappedBy = "waybill", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("signedAt asc")
    private List<WaybillSignature> signatures = new ArrayList<>();

    @OneToMany(mappedBy = "waybill", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt asc")
    private List<WaybillStatusHistory> statusHistory = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line1", column = @Column(name = "pickup_address_line1", length = 200)),
            @AttributeOverride(name = "line2", column = @Column(name = "pickup_address_line2", length = 200)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "pickup_address_postal_code", length = 20)),
            @AttributeOverride(name = "city", column = @Column(name = "pickup_address_city", length = 120)),
            @AttributeOverride(name = "state", column = @Column(name = "pickup_address_state", length = 120)),
            @AttributeOverride(name = "country", column = @Column(name = "pickup_address_country", length = 2))
    })
    private Address placeOfTakingOver = new Address();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude"))
    })
    private GeoPoint takingOverLocation = new GeoPoint();

    @Column(name = "pickup_client_id")
    private UUID pickupClientId;

    @Column(name = "pickup_client_address_id")
    private UUID pickupClientAddressId;

    @Column(name = "taking_over_planned_at")
    private Instant takingOverPlannedAt;

    @Column(name = "taking_over_actual_at")
    private Instant takingOverActualAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line1", column = @Column(name = "delivery_address_line1", length = 200)),
            @AttributeOverride(name = "line2", column = @Column(name = "delivery_address_line2", length = 200)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "delivery_address_postal_code", length = 20)),
            @AttributeOverride(name = "city", column = @Column(name = "delivery_address_city", length = 120)),
            @AttributeOverride(name = "state", column = @Column(name = "delivery_address_state", length = 120)),
            @AttributeOverride(name = "country", column = @Column(name = "delivery_address_country", length = 2))
    })
    private Address placeOfDelivery = new Address();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "delivery_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "delivery_longitude"))
    })
    private GeoPoint deliveryLocation = new GeoPoint();

    @Column(name = "delivery_client_id")
    private UUID deliveryClientId;

    @Column(name = "delivery_client_address_id")
    private UUID deliveryClientAddressId;

    @Column(name = "delivery_planned_at")
    private Instant deliveryPlannedAt;

    @Column(name = "delivery_actual_at")
    private Instant deliveryActualAt;

    @Column(name = "dock_entered_at")
    private Instant dockEnteredAt;

    @Column(name = "dock_exited_at")
    private Instant dockExitedAt;

    @Column(name = "attached_documents", columnDefinition = "text")
    private String attachedDocuments;

    @Column(name = "sender_instructions", columnDefinition = "text")
    private String senderInstructions;

    @Column(name = "carriage_charges_amount", precision = 12, scale = 2)
    private BigDecimal carriageChargesAmount;

    @Column(name = "carriage_charges_currency", length = 3)
    private String carriageChargesCurrency;

    @Column(name = "reservations_and_observations", columnDefinition = "text")
    private String reservationsAndObservations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "position_in_tour")
    private Integer positionInTour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_account_id")
    private Account assignedAccount;

    @Embedded
    private RouteInfo route = new RouteInfo();

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proof_of_delivery_image_id")
    private Image proofOfDeliveryImage;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean archived = false;

    @Column(name = "archived_at")
    private Instant archivedAt;

    public WaybillParty getParty(WaybillPartyRole role) {
        return parties.stream().filter(p -> p.getRole() == role).findFirst().orElse(null);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public WaybillStatus getStatus() {
        return status;
    }

    public void setStatus(WaybillStatus status) {
        this.status = status;
    }

    public WaybillScope getScope() {
        return scope;
    }

    public void setScope(WaybillScope scope) {
        this.scope = scope;
    }

    public List<WaybillParty> getParties() {
        return parties;
    }

    public List<WaybillGoodsLine> getGoodsLines() {
        return goodsLines;
    }

    public List<WaybillSignature> getSignatures() {
        return signatures;
    }

    public List<WaybillStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public Address getPlaceOfTakingOver() {
        return placeOfTakingOver;
    }

    public void setPlaceOfTakingOver(Address placeOfTakingOver) {
        this.placeOfTakingOver = placeOfTakingOver;
    }

    public GeoPoint getTakingOverLocation() {
        return takingOverLocation;
    }

    public void setTakingOverLocation(GeoPoint takingOverLocation) {
        this.takingOverLocation = takingOverLocation;
    }

    public UUID getPickupClientId() {
        return pickupClientId;
    }

    public void setPickupClientId(UUID pickupClientId) {
        this.pickupClientId = pickupClientId;
    }

    public UUID getPickupClientAddressId() {
        return pickupClientAddressId;
    }

    public void setPickupClientAddressId(UUID pickupClientAddressId) {
        this.pickupClientAddressId = pickupClientAddressId;
    }

    public Instant getTakingOverPlannedAt() {
        return takingOverPlannedAt;
    }

    public void setTakingOverPlannedAt(Instant takingOverPlannedAt) {
        this.takingOverPlannedAt = takingOverPlannedAt;
    }

    public Instant getTakingOverActualAt() {
        return takingOverActualAt;
    }

    public void setTakingOverActualAt(Instant takingOverActualAt) {
        this.takingOverActualAt = takingOverActualAt;
    }

    public Address getPlaceOfDelivery() {
        return placeOfDelivery;
    }

    public void setPlaceOfDelivery(Address placeOfDelivery) {
        this.placeOfDelivery = placeOfDelivery;
    }

    public GeoPoint getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(GeoPoint deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public UUID getDeliveryClientId() {
        return deliveryClientId;
    }

    public void setDeliveryClientId(UUID deliveryClientId) {
        this.deliveryClientId = deliveryClientId;
    }

    public UUID getDeliveryClientAddressId() {
        return deliveryClientAddressId;
    }

    public void setDeliveryClientAddressId(UUID deliveryClientAddressId) {
        this.deliveryClientAddressId = deliveryClientAddressId;
    }

    public Instant getDeliveryPlannedAt() {
        return deliveryPlannedAt;
    }

    public void setDeliveryPlannedAt(Instant deliveryPlannedAt) {
        this.deliveryPlannedAt = deliveryPlannedAt;
    }

    public Instant getDeliveryActualAt() {
        return deliveryActualAt;
    }

    public void setDeliveryActualAt(Instant deliveryActualAt) {
        this.deliveryActualAt = deliveryActualAt;
    }

    public Instant getDockEnteredAt() {
        return dockEnteredAt;
    }

    public void setDockEnteredAt(Instant dockEnteredAt) {
        this.dockEnteredAt = dockEnteredAt;
    }

    public Instant getDockExitedAt() {
        return dockExitedAt;
    }

    public void setDockExitedAt(Instant dockExitedAt) {
        this.dockExitedAt = dockExitedAt;
    }

    public String getAttachedDocuments() {
        return attachedDocuments;
    }

    public void setAttachedDocuments(String attachedDocuments) {
        this.attachedDocuments = attachedDocuments;
    }

    public String getSenderInstructions() {
        return senderInstructions;
    }

    public void setSenderInstructions(String senderInstructions) {
        this.senderInstructions = senderInstructions;
    }

    public BigDecimal getCarriageChargesAmount() {
        return carriageChargesAmount;
    }

    public void setCarriageChargesAmount(BigDecimal carriageChargesAmount) {
        this.carriageChargesAmount = carriageChargesAmount;
    }

    public String getCarriageChargesCurrency() {
        return carriageChargesCurrency;
    }

    public void setCarriageChargesCurrency(String carriageChargesCurrency) {
        this.carriageChargesCurrency = carriageChargesCurrency;
    }

    public String getReservationsAndObservations() {
        return reservationsAndObservations;
    }

    public void setReservationsAndObservations(String reservationsAndObservations) {
        this.reservationsAndObservations = reservationsAndObservations;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Integer getPositionInTour() {
        return positionInTour;
    }

    public void setPositionInTour(Integer positionInTour) {
        this.positionInTour = positionInTour;
    }

    public Account getAssignedAccount() {
        return assignedAccount;
    }

    public void setAssignedAccount(Account assignedAccount) {
        this.assignedAccount = assignedAccount;
    }

    public RouteInfo getRoute() {
        return route;
    }

    public void setRoute(RouteInfo route) {
        this.route = route;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Image getProofOfDeliveryImage() {
        return proofOfDeliveryImage;
    }

    public void setProofOfDeliveryImage(Image proofOfDeliveryImage) {
        this.proofOfDeliveryImage = proofOfDeliveryImage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }
}

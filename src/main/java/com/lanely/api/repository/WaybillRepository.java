package com.lanely.api.repository;

import com.lanely.api.entity.Waybill;
import com.lanely.api.entity.enums.WaybillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaybillRepository extends JpaRepository<Waybill, UUID> {

    Optional<Waybill> findByIdAndCompanyId(UUID id, UUID companyId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Waybill w set w.client = null where w.client.id = :clientId")
    void detachClient(@Param("clientId") UUID clientId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Waybill w set w.pickupClientId = null where w.pickupClientId = :clientId")
    void clearPickupClient(@Param("clientId") UUID clientId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Waybill w set w.deliveryClientId = null where w.deliveryClientId = :clientId")
    void clearDeliveryClient(@Param("clientId") UUID clientId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Waybill w set w.pickupClientAddressId = null where w.pickupClientAddressId in :addressIds")
    void clearPickupClientAddresses(@Param("addressIds") Collection<UUID> addressIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Waybill w set w.deliveryClientAddressId = null where w.deliveryClientAddressId in :addressIds")
    void clearDeliveryClientAddresses(@Param("addressIds") Collection<UUID> addressIds);

    boolean existsByCompanyIdAndReference(UUID companyId, String reference);

    long countByCompanyId(UUID companyId);

    List<Waybill> findByTourIdOrderByPositionInTourAsc(UUID tourId);

    @Query("select w from Waybill w where w.company.id = :companyId "
            + "and (:hasStatus = false or w.status in :statuses) "
            + "and (:archived is null or w.archived = :archived) "
            + "and (:tourId is null or w.tour.id = :tourId) "
            + "and (:assignedAccountId is null or w.assignedAccount.id = :assignedAccountId) "
            + "and (:clientId is null or w.client.id = :clientId) "
            + "and (cast(:pickupFrom as timestamp) is null or w.takingOverPlannedAt >= :pickupFrom) "
            + "and (cast(:pickupTo as timestamp) is null or w.takingOverPlannedAt < :pickupTo) "
            + "and (cast(:deliveryFrom as timestamp) is null or w.deliveryPlannedAt >= :deliveryFrom) "
            + "and (cast(:deliveryTo as timestamp) is null or w.deliveryPlannedAt < :deliveryTo) "
            + "and (cast(:dockFrom as timestamp) is null or w.dockEnteredAt >= :dockFrom) "
            + "and (cast(:dockTo as timestamp) is null or w.dockEnteredAt < :dockTo) "
            + "and (lower(w.reference) like :pattern "
            + "  or exists (select p from WaybillParty p where p.waybill = w and lower(p.name) like :pattern))")
    Page<Waybill> search(@Param("companyId") UUID companyId,
                         @Param("hasStatus") boolean hasStatus, @Param("statuses") Collection<WaybillStatus> statuses,
                         @Param("archived") Boolean archived,
                         @Param("tourId") UUID tourId, @Param("assignedAccountId") UUID assignedAccountId,
                         @Param("clientId") UUID clientId,
                         @Param("pickupFrom") Instant pickupFrom, @Param("pickupTo") Instant pickupTo,
                         @Param("deliveryFrom") Instant deliveryFrom, @Param("deliveryTo") Instant deliveryTo,
                         @Param("dockFrom") Instant dockFrom, @Param("dockTo") Instant dockTo,
                         @Param("pattern") String pattern, Pageable pageable);

    @Query("select distinct w from Waybill w where w.company.id = :companyId and w.archived = false "
            + "and (w.status = com.lanely.api.entity.enums.WaybillStatus.AT_DOCK "
            + "  or exists (select g from WaybillGoodsLine g where g.waybill = w "
            + "    and g.status = com.lanely.api.entity.enums.ParcelStatus.AT_DOCK)) "
            + "and (:clientId is null or w.client.id = :clientId) "
            + "and (cast(:dockFrom as timestamp) is null or w.dockEnteredAt >= :dockFrom) "
            + "and (cast(:dockTo as timestamp) is null or w.dockEnteredAt < :dockTo) "
            + "and (lower(w.reference) like :pattern "
            + "  or exists (select p from WaybillParty p where p.waybill = w and lower(p.name) like :pattern))")
    Page<Waybill> searchAtDock(@Param("companyId") UUID companyId,
                               @Param("clientId") UUID clientId,
                               @Param("dockFrom") Instant dockFrom, @Param("dockTo") Instant dockTo,
                               @Param("pattern") String pattern, Pageable pageable);

    @Query("select count(w) from Waybill w where w.company.id = :companyId and w.archived = false "
            + "and (w.status = com.lanely.api.entity.enums.WaybillStatus.AT_DOCK "
            + "  or exists (select g from WaybillGoodsLine g where g.waybill = w "
            + "    and g.status = com.lanely.api.entity.enums.ParcelStatus.AT_DOCK))")
    long countAtDock(@Param("companyId") UUID companyId);

    @Query("select coalesce(sum(g.numberOfPackages), 0), coalesce(sum(g.grossWeightKg), 0), coalesce(sum(g.volumeM3), 0) "
            + "from WaybillGoodsLine g where g.waybill.company.id = :companyId and g.waybill.archived = false "
            + "and (g.status = com.lanely.api.entity.enums.ParcelStatus.AT_DOCK "
            + "  or g.waybill.status = com.lanely.api.entity.enums.WaybillStatus.AT_DOCK)")
    List<Object[]> sumGoodsAtDock(@Param("companyId") UUID companyId);

    @Query("select w from Waybill w "
            + "left join w.tour t "
            + "left join t.assignedAccount ta "
            + "where w.company.id = :companyId "
            + "and (:status is null or w.status = :status) "
            + "and (w.assignedAccount.id = :accountId or ta.id = :accountId) "
            + "and (cast(:pickupFrom as timestamp) is null or w.takingOverPlannedAt >= :pickupFrom) "
            + "and (cast(:pickupTo as timestamp) is null or w.takingOverPlannedAt < :pickupTo) "
            + "and (cast(:deliveryFrom as timestamp) is null or w.deliveryPlannedAt >= :deliveryFrom) "
            + "and (cast(:deliveryTo as timestamp) is null or w.deliveryPlannedAt < :deliveryTo) "
            + "and (cast(:dockFrom as timestamp) is null or w.dockEnteredAt >= :dockFrom) "
            + "and (cast(:dockTo as timestamp) is null or w.dockEnteredAt < :dockTo)")
    Page<Waybill> findAssignedToAccount(@Param("companyId") UUID companyId, @Param("accountId") UUID accountId,
                                        @Param("status") WaybillStatus status,
                                        @Param("pickupFrom") Instant pickupFrom, @Param("pickupTo") Instant pickupTo,
                                        @Param("deliveryFrom") Instant deliveryFrom, @Param("deliveryTo") Instant deliveryTo,
                                        @Param("dockFrom") Instant dockFrom, @Param("dockTo") Instant dockTo,
                                        Pageable pageable);

    @Query("select w from Waybill w "
            + "left join w.tour t "
            + "left join t.assignedAccount ta "
            + "where w.id = :id and w.company.id = :companyId "
            + "and (w.assignedAccount.id = :accountId or ta.id = :accountId)")
    Optional<Waybill> findAssignedToAccount(@Param("id") UUID id, @Param("companyId") UUID companyId,
                                            @Param("accountId") UUID accountId);
}

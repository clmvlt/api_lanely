package com.lanely.api.repository;

import com.lanely.api.entity.WaybillGoodsLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WaybillGoodsLineRepository extends JpaRepository<WaybillGoodsLine, UUID> {

    @Query("select g from WaybillGoodsLine g where g.id = :id and g.waybill.id = :waybillId "
            + "and g.waybill.company.id = :companyId")
    Optional<WaybillGoodsLine> findScoped(@Param("id") UUID id, @Param("waybillId") UUID waybillId,
                                          @Param("companyId") UUID companyId);

    @Query("select g from WaybillGoodsLine g "
            + "join g.waybill w "
            + "left join w.tour t "
            + "left join t.assignedAccount ta "
            + "where g.id = :id and w.id = :waybillId "
            + "and w.company.id = :companyId "
            + "and (w.assignedAccount.id = :accountId or ta.id = :accountId)")
    Optional<WaybillGoodsLine> findScopedForAssignee(@Param("id") UUID id, @Param("waybillId") UUID waybillId,
                                                     @Param("companyId") UUID companyId,
                                                     @Param("accountId") UUID accountId);
}

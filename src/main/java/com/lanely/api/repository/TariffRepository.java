package com.lanely.api.repository;

import com.lanely.api.entity.Tariff;
import com.lanely.api.entity.enums.TariffStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TariffRepository extends JpaRepository<Tariff, UUID> {

    Optional<Tariff> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("select t from Tariff t where t.company.id = :companyId "
            + "and (:status is null or t.status = :status) "
            + "and (:clientId is null or t.client.id = :clientId)")
    Page<Tariff> search(@Param("companyId") UUID companyId, @Param("status") TariffStatus status,
                        @Param("clientId") UUID clientId, Pageable pageable);

    @Query("select t from Tariff t where t.company.id = :companyId and t.status = :status "
            + "and t.client.id = :clientId "
            + "and (t.validFrom is null or t.validFrom <= :onDate) "
            + "and (t.validUntil is null or t.validUntil >= :onDate) "
            + "order by t.validFrom desc nulls last")
    List<Tariff> findClientTariffs(@Param("companyId") UUID companyId, @Param("clientId") UUID clientId,
                                   @Param("status") TariffStatus status, @Param("onDate") LocalDate onDate);

    @Query("select t from Tariff t where t.company.id = :companyId and t.status = :status and t.isDefault = true "
            + "and (t.validFrom is null or t.validFrom <= :onDate) "
            + "and (t.validUntil is null or t.validUntil >= :onDate) "
            + "order by t.validFrom desc nulls last")
    List<Tariff> findDefaultTariffs(@Param("companyId") UUID companyId, @Param("status") TariffStatus status,
                                    @Param("onDate") LocalDate onDate);

    List<Tariff> findByCompanyIdAndIsDefaultTrueAndStatus(UUID companyId, TariffStatus status);
}

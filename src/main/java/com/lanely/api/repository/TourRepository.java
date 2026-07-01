package com.lanely.api.repository;

import com.lanely.api.entity.Tour;
import com.lanely.api.entity.enums.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TourRepository extends JpaRepository<Tour, UUID> {

    Optional<Tour> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndReference(UUID companyId, String reference);

    long countByCompanyId(UUID companyId);

    @Query("select t from Tour t where t.company.id = :companyId "
            + "and (:status is null or t.status = :status) "
            + "and (:plannedDate is null or t.plannedDate = :plannedDate) "
            + "and (:assignedAccountId is null or t.assignedAccount.id = :assignedAccountId) "
            + "and (lower(t.reference) like :pattern or lower(t.name) like :pattern)")
    Page<Tour> search(@Param("companyId") UUID companyId, @Param("status") TourStatus status,
                      @Param("plannedDate") LocalDate plannedDate, @Param("assignedAccountId") UUID assignedAccountId,
                      @Param("pattern") String pattern, Pageable pageable);

    @Query("select t from Tour t where t.company.id = :companyId and t.assignedAccount.id = :accountId "
            + "and (:status is null or t.status = :status)")
    Page<Tour> findAssignedToAccount(@Param("companyId") UUID companyId, @Param("accountId") UUID accountId,
                                     @Param("status") TourStatus status, Pageable pageable);

    Optional<Tour> findByIdAndCompanyIdAndAssignedAccountId(UUID id, UUID companyId, UUID assignedAccountId);
}

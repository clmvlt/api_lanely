package com.lanely.api.repository;

import com.lanely.api.entity.Client;
import com.lanely.api.entity.enums.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndReference(UUID companyId, String reference);

    long countByCompanyId(UUID companyId);

    @Query("select c from Client c where c.company.id = :companyId "
            + "and (:status is null or c.status = :status) "
            + "and (lower(c.name) like :pattern "
            + "  or lower(c.reference) like :pattern "
            + "  or lower(coalesce(c.email, '')) like :pattern)")
    Page<Client> search(@Param("companyId") UUID companyId, @Param("status") ClientStatus status,
                        @Param("pattern") String pattern, Pageable pageable);
}

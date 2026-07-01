package com.lanely.api.repository;

import com.lanely.api.entity.GoodsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GoodsTypeRepository extends JpaRepository<GoodsType, UUID> {

    Optional<GoodsType> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndNameIgnoreCase(UUID companyId, String name);

    @Query("select g from GoodsType g where g.company.id = :companyId "
            + "and (lower(g.name) like :pattern "
            + "  or lower(coalesce(g.description, '')) like :pattern)")
    Page<GoodsType> search(@Param("companyId") UUID companyId, @Param("pattern") String pattern, Pageable pageable);
}

package com.lanely.api.repository;

import com.lanely.api.entity.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {

    Optional<CompanyMember> findByCompanyIdAndUserId(UUID companyId, UUID userId);

    boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId);

    List<CompanyMember> findByCompanyId(UUID companyId);

    List<CompanyMember> findByUserId(UUID userId);

    long countByCompanyId(UUID companyId);
}

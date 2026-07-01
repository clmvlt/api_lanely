package com.lanely.api.repository;

import com.lanely.api.entity.CompanyInvitation;
import com.lanely.api.entity.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyInvitationRepository extends JpaRepository<CompanyInvitation, UUID> {

    Optional<CompanyInvitation> findByCode(String code);

    boolean existsByCompanyIdAndEmailIgnoreCaseAndStatus(UUID companyId, String email, InvitationStatus status);

    List<CompanyInvitation> findByCompanyId(UUID companyId);
}

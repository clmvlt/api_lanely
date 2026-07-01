package com.lanely.api.repository;

import com.lanely.api.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByCompanyIdAndUsername(UUID companyId, String username);

    boolean existsByCompanyIdAndUsername(UUID companyId, String username);

    List<Profile> findByCompanyId(UUID companyId);

    long countByCompanyIdAndActiveTrue(UUID companyId);
}

package com.lanely.api.repository;

import com.lanely.api.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByPublicCode(String publicCode);

    boolean existsByPublicCode(String publicCode);

    long countByOwnerId(UUID ownerId);

    List<Company> findByOwnerId(UUID ownerId);
}

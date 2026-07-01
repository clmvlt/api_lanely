package com.lanely.api.repository;

import com.lanely.api.entity.TariffComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TariffComponentRepository extends JpaRepository<TariffComponent, UUID> {

    Optional<TariffComponent> findByIdAndTariffId(UUID id, UUID tariffId);
}

package com.lanely.api.repository;

import com.lanely.api.entity.ClientAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientAddressRepository extends JpaRepository<ClientAddress, UUID> {

    List<ClientAddress> findByClientIdOrderByCreatedAtAsc(UUID clientId);

    Optional<ClientAddress> findByIdAndClientId(UUID id, UUID clientId);

    void deleteByClientId(UUID clientId);
}

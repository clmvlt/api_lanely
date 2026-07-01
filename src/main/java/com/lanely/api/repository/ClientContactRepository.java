package com.lanely.api.repository;

import com.lanely.api.entity.ClientContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientContactRepository extends JpaRepository<ClientContact, UUID> {

    List<ClientContact> findByClientIdOrderByCreatedAtAsc(UUID clientId);

    Optional<ClientContact> findByIdAndClientId(UUID id, UUID clientId);

    void deleteByClientId(UUID clientId);
}

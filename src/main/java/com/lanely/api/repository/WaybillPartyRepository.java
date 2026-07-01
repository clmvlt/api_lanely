package com.lanely.api.repository;

import com.lanely.api.entity.WaybillParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.UUID;

public interface WaybillPartyRepository extends JpaRepository<WaybillParty, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update WaybillParty p set p.clientId = null where p.clientId = :clientId")
    void clearClient(@Param("clientId") UUID clientId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update WaybillParty p set p.clientAddressId = null where p.clientAddressId in :addressIds")
    void clearClientAddresses(@Param("addressIds") Collection<UUID> addressIds);
}

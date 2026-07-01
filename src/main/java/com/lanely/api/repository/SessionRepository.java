package com.lanely.api.repository;

import com.lanely.api.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    Optional<Session> findByIdAndAccountId(UUID id, UUID accountId);

    boolean existsByIdAndAccountId(UUID id, UUID accountId);

    List<Session> findByAccountId(UUID accountId);

    void deleteByAccountId(UUID accountId);

    void deleteByAccountIdAndIdNot(UUID accountId, UUID id);
}

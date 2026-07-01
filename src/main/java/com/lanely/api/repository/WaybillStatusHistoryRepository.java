package com.lanely.api.repository;

import com.lanely.api.entity.WaybillStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WaybillStatusHistoryRepository extends JpaRepository<WaybillStatusHistory, UUID> {

    Page<WaybillStatusHistory> findByWaybillIdOrderByChangedAtDesc(UUID waybillId, Pageable pageable);
}

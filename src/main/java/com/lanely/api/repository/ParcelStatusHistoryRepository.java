package com.lanely.api.repository;

import com.lanely.api.entity.ParcelStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParcelStatusHistoryRepository extends JpaRepository<ParcelStatusHistory, UUID> {

    Page<ParcelStatusHistory> findByParcelIdOrderByChangedAtDesc(UUID parcelId, Pageable pageable);
}

package com.lanely.api.repository;

import com.lanely.api.entity.TourStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TourStatusHistoryRepository extends JpaRepository<TourStatusHistory, UUID> {

    Page<TourStatusHistory> findByTourIdOrderByChangedAtDesc(UUID tourId, Pageable pageable);
}

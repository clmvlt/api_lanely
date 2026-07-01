package com.lanely.api.service;

import com.lanely.api.config.FuelProperties;
import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.fuel.FuelPriceResponse;
import com.lanely.api.dto.fuel.RefreshFuelPricesResponse;
import com.lanely.api.entity.FuelPriceIndex;
import com.lanely.api.entity.enums.FuelType;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.mapper.CompanyMapper;
import com.lanely.api.mapper.FuelPriceMapper;
import com.lanely.api.repository.FuelPriceIndexRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FuelPriceService {

    private final CompanyService companyService;
    private final FuelPriceIndexRepository repository;
    private final FuelPriceRefreshService refreshService;
    private final FuelProperties properties;

    public FuelPriceService(CompanyService companyService, FuelPriceIndexRepository repository,
                            FuelPriceRefreshService refreshService, FuelProperties properties) {
        this.companyService = companyService;
        this.repository = repository;
        this.refreshService = refreshService;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public FuelPriceResponse current(UUID currentUserId, UUID companyId, FuelType fuelType, String source) {
        companyService.requireMember(companyId, currentUserId);
        FuelType type = fuelType == null ? properties.fuelTypeOrDefault() : fuelType;
        String src = CompanyMapper.blankToNull(source);
        FuelPriceIndex index = (src == null
                ? repository.findFirstByFuelTypeOrderByReferenceDateDesc(type)
                : repository.findFirstByFuelTypeAndSourceOrderByReferenceDateDesc(type, src))
                .orElseThrow(() -> new ResourceNotFoundException("error.fuel.no-index"));
        return FuelPriceMapper.toResponse(index);
    }

    @Transactional(readOnly = true)
    public PageResponse<FuelPriceResponse> history(UUID currentUserId, UUID companyId, FuelType fuelType, String source,
                                                   LocalDate from, LocalDate to, Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        FuelType type = fuelType == null ? properties.fuelTypeOrDefault() : fuelType;
        Page<FuelPriceResponse> page = repository
                .history(type, CompanyMapper.blankToNull(source), from, to, pageable)
                .map(FuelPriceMapper::toResponse);
        return PageResponse.of(page);
    }

    @Transactional
    public RefreshFuelPricesResponse refresh(UUID currentUserId, UUID companyId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        int ingested = refreshService.refresh();
        return new RefreshFuelPricesResponse(properties.defaultSourceOrDefault(), ingested, Instant.now());
    }
}

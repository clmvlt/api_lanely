package com.lanely.api.service;

import com.lanely.api.dto.pricing.QuoteInputsDto;
import com.lanely.api.dto.pricing.QuoteLineDto;
import com.lanely.api.dto.pricing.QuoteRequest;
import com.lanely.api.dto.pricing.QuoteResponse;
import com.lanely.api.entity.Client;
import com.lanely.api.entity.FuelPriceIndex;
import com.lanely.api.entity.FuelSurchargeComponent;
import com.lanely.api.entity.FuelSurchargePolicy;
import com.lanely.api.entity.Tariff;
import com.lanely.api.entity.TariffComponent;
import com.lanely.api.entity.Waybill;
import com.lanely.api.entity.WaybillGoodsLine;
import com.lanely.api.entity.embeddable.RouteInfo;
import com.lanely.api.entity.enums.ComponentKind;
import com.lanely.api.entity.enums.FuelSurchargeMode;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.entity.enums.PricingBasis;
import com.lanely.api.entity.enums.TariffStatus;
import com.lanely.api.entity.enums.WaybillStatus;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.mapper.CompanyMapper;
import com.lanely.api.repository.FuelPriceIndexRepository;
import com.lanely.api.repository.TariffRepository;
import com.lanely.api.repository.WaybillRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PricingService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal METERS_PER_KM = BigDecimal.valueOf(1000);

    private final CompanyService companyService;
    private final TariffRepository tariffRepository;
    private final FuelPriceIndexRepository fuelPriceIndexRepository;
    private final WaybillRepository waybillRepository;
    private final MessageSource messageSource;

    public PricingService(CompanyService companyService, TariffRepository tariffRepository,
                          FuelPriceIndexRepository fuelPriceIndexRepository, WaybillRepository waybillRepository,
                          MessageSource messageSource) {
        this.companyService = companyService;
        this.tariffRepository = tariffRepository;
        this.fuelPriceIndexRepository = fuelPriceIndexRepository;
        this.waybillRepository = waybillRepository;
        this.messageSource = messageSource;
    }

    // ----- Public API -----

    @Transactional(readOnly = true)
    public QuoteResponse quote(UUID currentUserId, UUID companyId, QuoteRequest request) {
        companyService.requireMember(companyId, currentUserId);
        List<String> warnings = new ArrayList<>();

        PricingInputs inputs;
        UUID clientId;
        if (request.waybillId() != null) {
            Waybill waybill = loadWaybill(companyId, request.waybillId());
            inputs = inputsFromWaybill(waybill, warnings);
            clientId = clientId(waybill.getClient());
        } else {
            inputs = inputsFromDto(request.inputs());
            clientId = request.clientId();
        }

        Tariff tariff = resolveTariff(companyId, clientId, request.tariffId(), inputs.referenceDate());
        return compute(tariff, inputs, warnings);
    }

    @Transactional
    public QuoteResponse recalculateWaybill(UUID currentUserId, UUID companyId, UUID waybillId, UUID tariffIdOverride) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Waybill waybill = loadWaybill(companyId, waybillId);
        if (waybill.getStatus() == WaybillStatus.CANCELLED) {
            throw new BadRequestException("error.pricing.not-applicable");
        }

        List<String> warnings = new ArrayList<>();
        PricingInputs inputs = inputsFromWaybill(waybill, warnings);
        Tariff tariff = resolveTariff(companyId, clientId(waybill.getClient()), tariffIdOverride, inputs.referenceDate());
        QuoteResponse quote = compute(tariff, inputs, warnings);

        waybill.setCarriageChargesAmount(quote.total());
        waybill.setCarriageChargesCurrency(quote.currency());
        return quote;
    }

    // ----- Grid resolution -----

    private Tariff resolveTariff(UUID companyId, UUID clientId, UUID explicitTariffId, LocalDate referenceDate) {
        if (explicitTariffId != null) {
            return tariffRepository.findByIdAndCompanyId(explicitTariffId, companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.tariff.not-found"));
        }
        if (clientId != null) {
            List<Tariff> clientTariffs = tariffRepository
                    .findClientTariffs(companyId, clientId, TariffStatus.ACTIVE, referenceDate);
            if (!clientTariffs.isEmpty()) {
                return clientTariffs.get(0);
            }
        }
        List<Tariff> defaults = tariffRepository.findDefaultTariffs(companyId, TariffStatus.ACTIVE, referenceDate);
        if (!defaults.isEmpty()) {
            return defaults.get(0);
        }
        throw new BadRequestException("error.pricing.no-tariff");
    }

    // ----- Computation -----

    private QuoteResponse compute(Tariff tariff, PricingInputs inputs, List<String> warnings) {
        int scale = tariff.getRoundingScale();
        RoundingMode rounding = tariff.getRoundingMode();
        List<QuoteLineDto> lines = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (TariffComponent component : tariff.getComponents()) {
            LineResult line = computeComponentLine(component.getBasis(), component.getUnitPrice(),
                    component.getIncludedQuantity(), component.getMinQuantity(), component.getMaxQuantity(),
                    component.getMinAmount(), component.getMaxAmount(), inputs, subtotal, scale, rounding);
            subtotal = subtotal.add(line.amount());
            lines.add(new QuoteLineDto(component.getId(), component.getLabel(), component.getBasis(),
                    ComponentKind.BASE, component.getUnitPrice(), line.quantity(), line.amount()));
        }
        subtotal = subtotal.setScale(scale, rounding);

        BigDecimal surchargeTotal = BigDecimal.ZERO;
        BigDecimal fuelPriceUsed = null;
        LocalDate fuelReferenceDate = null;

        FuelSurchargePolicy policy = tariff.getFuelSurcharge();
        if (policy != null && policy.isEnabled()) {
            Optional<FuelPriceIndex> indexOpt = latestIndex(policy);
            if (indexOpt.isEmpty()) {
                warnings.add("error.fuel.no-index");
            } else {
                FuelPriceIndex index = indexOpt.get();
                fuelPriceUsed = index.getPrice();
                fuelReferenceDate = index.getReferenceDate();
                surchargeTotal = applyFuelSurcharge(policy, index.getPrice(), subtotal, inputs, lines, scale, rounding);
            }
        }
        surchargeTotal = surchargeTotal.setScale(scale, rounding);

        BigDecimal total = subtotal.add(surchargeTotal);
        if (tariff.getMinChargeAmount() != null && total.compareTo(tariff.getMinChargeAmount()) < 0) {
            total = tariff.getMinChargeAmount();
        }
        total = total.setScale(scale, rounding);

        return new QuoteResponse(tariff.getId(), tariff.getName(), tariff.getCurrency(), lines, subtotal,
                surchargeTotal, total, fuelPriceUsed, fuelReferenceDate, warnings);
    }

    private BigDecimal applyFuelSurcharge(FuelSurchargePolicy policy, BigDecimal currentPrice, BigDecimal subtotal,
                                          PricingInputs inputs, List<QuoteLineDto> lines, int scale,
                                          RoundingMode rounding) {
        if (policy.getMode() == FuelSurchargeMode.THRESHOLD_COMPONENTS) {
            if (policy.getThresholdPrice() != null && currentPrice.compareTo(policy.getThresholdPrice()) <= 0) {
                return BigDecimal.ZERO;
            }
            BigDecimal total = BigDecimal.ZERO;
            for (FuelSurchargeComponent component : policy.getSurchargeComponents()) {
                LineResult line = computeComponentLine(component.getBasis(), component.getUnitPrice(),
                        component.getIncludedQuantity(), component.getMinQuantity(), component.getMaxQuantity(),
                        component.getMinAmount(), component.getMaxAmount(), inputs, subtotal, scale, rounding);
                total = total.add(line.amount());
                lines.add(new QuoteLineDto(component.getId(), component.getLabel(), component.getBasis(),
                        ComponentKind.SURCHARGE, component.getUnitPrice(), line.quantity(), line.amount()));
            }
            return total;
        }

        // INDEXED_PERCENT: surcharge = subtotal * dieselShare * (current - reference) / reference
        BigDecimal reference = policy.getReferencePrice();
        BigDecimal share = policy.getDieselShareRatio();
        if (reference == null || reference.signum() == 0 || share == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = share.multiply(currentPrice.subtract(reference))
                .divide(reference, 10, RoundingMode.HALF_UP);
        BigDecimal amount = subtotal.multiply(rate).setScale(scale, rounding);
        if (policy.isClampAtZero() && amount.signum() < 0) {
            amount = BigDecimal.ZERO;
        }
        String label = messageSource.getMessage("pricing.line.fuel-surcharge", null, LocaleContextHolder.getLocale());
        lines.add(new QuoteLineDto(null, label, PricingBasis.PERCENT_OF_SUBTOTAL, ComponentKind.SURCHARGE,
                rate.movePointRight(2).setScale(2, RoundingMode.HALF_UP), subtotal, amount));
        return amount;
    }

    private LineResult computeComponentLine(PricingBasis basis, BigDecimal unitPrice, BigDecimal includedQuantity,
                                            BigDecimal minQuantity, BigDecimal maxQuantity, BigDecimal minAmount,
                                            BigDecimal maxAmount, PricingInputs inputs, BigDecimal subtotal, int scale,
                                            RoundingMode rounding) {
        if (basis == PricingBasis.PERCENT_OF_SUBTOTAL) {
            BigDecimal amount = subtotal.multiply(unitPrice).divide(HUNDRED, scale, rounding);
            amount = clampAmount(amount, minAmount, maxAmount).setScale(scale, rounding);
            return new LineResult(subtotal, amount);
        }
        BigDecimal rawQty = rawQuantity(basis, inputs);
        BigDecimal qty = rawQty;
        if (includedQuantity != null) {
            qty = qty.subtract(includedQuantity);
        }
        if (qty.signum() < 0) {
            qty = BigDecimal.ZERO;
        }
        if (minQuantity != null && qty.compareTo(minQuantity) < 0) {
            qty = minQuantity;
        }
        if (maxQuantity != null && qty.compareTo(maxQuantity) > 0) {
            qty = maxQuantity;
        }
        BigDecimal amount = clampAmount(unitPrice.multiply(qty), minAmount, maxAmount).setScale(scale, rounding);
        return new LineResult(qty, amount);
    }

    private BigDecimal clampAmount(BigDecimal amount, BigDecimal minAmount, BigDecimal maxAmount) {
        BigDecimal result = amount;
        if (minAmount != null && result.compareTo(minAmount) < 0) {
            result = minAmount;
        }
        if (maxAmount != null && result.compareTo(maxAmount) > 0) {
            result = maxAmount;
        }
        return result;
    }

    private BigDecimal rawQuantity(PricingBasis basis, PricingInputs inputs) {
        return switch (basis) {
            case FLAT -> BigDecimal.ONE;
            case PER_WAYBILL -> BigDecimal.valueOf(inputs.waybillCount());
            case PER_STOP -> BigDecimal.valueOf(inputs.stopCount());
            case PER_PACKAGE -> BigDecimal.valueOf(inputs.packageCount());
            case PER_KM -> inputs.distanceKm() == null ? BigDecimal.ZERO : inputs.distanceKm();
            case PER_KG -> inputs.totalWeightKg();
            case PER_M3 -> inputs.totalVolumeM3();
            case PERCENT_OF_SUBTOTAL -> BigDecimal.ZERO;
        };
    }

    private Optional<FuelPriceIndex> latestIndex(FuelSurchargePolicy policy) {
        String source = CompanyMapper.blankToNull(policy.getSourceFilter());
        return source == null
                ? fuelPriceIndexRepository.findFirstByFuelTypeOrderByReferenceDateDesc(policy.getFuelType())
                : fuelPriceIndexRepository.findFirstByFuelTypeAndSourceOrderByReferenceDateDesc(policy.getFuelType(), source);
    }

    // ----- Inputs -----

    private PricingInputs inputsFromWaybill(Waybill waybill, List<String> warnings) {
        BigDecimal distanceKm = null;
        RouteInfo route = waybill.getRoute();
        if (route != null && route.isComputed() && route.getDistanceMeters() != null) {
            distanceKm = BigDecimal.valueOf(route.getDistanceMeters())
                    .divide(METERS_PER_KM, 3, RoundingMode.HALF_UP);
        } else {
            warnings.add("error.pricing.route-not-computed");
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        int packages = 0;
        for (WaybillGoodsLine line : waybill.getGoodsLines()) {
            if (line.getGrossWeightKg() != null) {
                totalWeight = totalWeight.add(line.getGrossWeightKg());
            }
            if (line.getVolumeM3() != null) {
                totalVolume = totalVolume.add(line.getVolumeM3());
            }
            if (line.getNumberOfPackages() != null) {
                packages += line.getNumberOfPackages();
            }
        }
        return new PricingInputs(distanceKm, totalWeight, totalVolume, packages, 1, 1, today());
    }

    private PricingInputs inputsFromDto(QuoteInputsDto dto) {
        if (dto == null) {
            return new PricingInputs(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 1, 1, today());
        }
        return new PricingInputs(
                dto.distanceKm() == null ? BigDecimal.ZERO : dto.distanceKm(),
                dto.totalWeightKg() == null ? BigDecimal.ZERO : dto.totalWeightKg(),
                dto.totalVolumeM3() == null ? BigDecimal.ZERO : dto.totalVolumeM3(),
                dto.packageCount() == null ? 0 : dto.packageCount(),
                dto.stopCount() == null ? 1 : dto.stopCount(),
                1, today());
    }

    private LocalDate today() {
        return LocalDate.ofInstant(Instant.now(), ZoneOffset.UTC);
    }

    private UUID clientId(Client client) {
        return client == null ? null : client.getId();
    }

    private Waybill loadWaybill(UUID companyId, UUID waybillId) {
        return waybillRepository.findByIdAndCompanyId(waybillId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.waybill.not-found"));
    }

    private record PricingInputs(BigDecimal distanceKm, BigDecimal totalWeightKg, BigDecimal totalVolumeM3,
                                 int packageCount, int stopCount, int waybillCount, LocalDate referenceDate) {
    }

    private record LineResult(BigDecimal quantity, BigDecimal amount) {
    }
}

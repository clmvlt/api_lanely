package com.lanely.api.service;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.pricing.CreateTariffComponentRequest;
import com.lanely.api.dto.pricing.CreateTariffRequest;
import com.lanely.api.dto.pricing.FuelSurchargeComponentRequest;
import com.lanely.api.dto.pricing.FuelSurchargePolicyResponse;
import com.lanely.api.dto.pricing.TariffResponse;
import com.lanely.api.dto.pricing.TariffSummaryResponse;
import com.lanely.api.dto.pricing.UpdateTariffComponentRequest;
import com.lanely.api.dto.pricing.UpdateTariffRequest;
import com.lanely.api.dto.pricing.UpsertFuelSurchargePolicyRequest;
import com.lanely.api.entity.Client;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.FuelSurchargeComponent;
import com.lanely.api.entity.FuelSurchargePolicy;
import com.lanely.api.entity.Tariff;
import com.lanely.api.entity.TariffComponent;
import com.lanely.api.entity.enums.ComponentKind;
import com.lanely.api.entity.enums.FuelSurchargeMode;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.entity.enums.TariffStatus;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.mapper.CompanyMapper;
import com.lanely.api.mapper.FuelSurchargePolicyMapper;
import com.lanely.api.mapper.TariffMapper;
import com.lanely.api.repository.ClientRepository;
import com.lanely.api.repository.FuelSurchargePolicyRepository;
import com.lanely.api.repository.TariffComponentRepository;
import com.lanely.api.repository.TariffRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TariffService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "status", "createdAt", "updatedAt");
    private static final UUID DETACH_CLIENT = new UUID(0L, 0L);

    private final CompanyService companyService;
    private final TariffRepository tariffRepository;
    private final TariffComponentRepository componentRepository;
    private final FuelSurchargePolicyRepository policyRepository;
    private final ClientRepository clientRepository;

    public TariffService(CompanyService companyService, TariffRepository tariffRepository,
                         TariffComponentRepository componentRepository,
                         FuelSurchargePolicyRepository policyRepository, ClientRepository clientRepository) {
        this.companyService = companyService;
        this.tariffRepository = tariffRepository;
        this.componentRepository = componentRepository;
        this.policyRepository = policyRepository;
        this.clientRepository = clientRepository;
    }

    // ----- Tariffs -----

    @Transactional
    public TariffResponse createTariff(UUID currentUserId, UUID companyId, CreateTariffRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        Company company = membership.getCompany();

        Tariff tariff = new Tariff();
        tariff.setCompany(company);
        tariff.setName(request.name().trim());
        tariff.setDescription(CompanyMapper.blankToNull(request.description()));
        tariff.setCurrency(currencyOrDefault(request.currency()));
        tariff.setStatus(TariffStatus.DRAFT);
        boolean isDefault = Boolean.TRUE.equals(request.isDefault());
        Client client = resolveClient(companyId, request.clientId());
        if (isDefault && client != null) {
            throw new BadRequestException("error.tariff.client-default");
        }
        tariff.setClient(client);
        tariff.setDefault(isDefault);
        validateValidity(request.validFrom(), request.validUntil());
        tariff.setValidFrom(request.validFrom());
        tariff.setValidUntil(request.validUntil());
        tariff.setRoundingMode(request.roundingMode() == null ? RoundingMode.HALF_UP : request.roundingMode());
        tariff.setRoundingScale(request.roundingScale() == null ? 2 : request.roundingScale());
        tariff.setMinChargeAmount(request.minChargeAmount());

        // A DRAFT tariff is never the active default yet, so the single-active-default invariant is
        // only enforced on activation.
        tariffRepository.save(tariff);
        return TariffMapper.toResponse(tariff);
    }

    @Transactional(readOnly = true)
    public PageResponse<TariffSummaryResponse> listTariffs(UUID currentUserId, UUID companyId, TariffStatus status,
                                                           UUID clientId, Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        validateSort(pageable.getSort());
        Page<TariffSummaryResponse> page = tariffRepository.search(companyId, status, clientId, pageable)
                .map(TariffMapper::toSummary);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public TariffResponse getTariff(UUID currentUserId, UUID companyId, UUID tariffId) {
        companyService.requireMember(companyId, currentUserId);
        return TariffMapper.toResponse(loadTariff(companyId, tariffId));
    }

    @Transactional
    public TariffResponse updateTariff(UUID currentUserId, UUID companyId, UUID tariffId, UpdateTariffRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        Tariff tariff = loadTariff(companyId, tariffId);

        if (request.name() != null) {
            tariff.setName(request.name().trim());
        }
        if (request.description() != null) {
            tariff.setDescription(CompanyMapper.blankToNull(request.description()));
        }
        if (request.currency() != null) {
            tariff.setCurrency(currencyOrDefault(request.currency()));
        }
        if (request.clientId() != null) {
            tariff.setClient(DETACH_CLIENT.equals(request.clientId())
                    ? null : resolveClient(companyId, request.clientId()));
        }
        if (request.isDefault() != null) {
            tariff.setDefault(request.isDefault());
        }
        if (tariff.isDefault() && tariff.getClient() != null) {
            throw new BadRequestException("error.tariff.client-default");
        }
        if (request.validFrom() != null) {
            tariff.setValidFrom(request.validFrom());
        }
        if (request.validUntil() != null) {
            tariff.setValidUntil(request.validUntil());
        }
        validateValidity(tariff.getValidFrom(), tariff.getValidUntil());
        if (request.roundingMode() != null) {
            tariff.setRoundingMode(request.roundingMode());
        }
        if (request.roundingScale() != null) {
            tariff.setRoundingScale(request.roundingScale());
        }
        if (request.minChargeAmount() != null) {
            tariff.setMinChargeAmount(request.minChargeAmount());
        }
        if (tariff.isDefault() && tariff.getStatus() == TariffStatus.ACTIVE) {
            ensureNoOtherActiveDefault(companyId, tariff.getId());
        }
        return TariffMapper.toResponse(tariff);
    }

    @Transactional
    public TariffResponse setStatus(UUID currentUserId, UUID companyId, UUID tariffId, TariffStatus status) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        Tariff tariff = loadTariff(companyId, tariffId);
        if (status == TariffStatus.ACTIVE && tariff.isDefault()) {
            ensureNoOtherActiveDefault(companyId, tariff.getId());
        }
        tariff.setStatus(status);
        return TariffMapper.toResponse(tariff);
    }

    @Transactional
    public void deleteTariff(UUID currentUserId, UUID companyId, UUID tariffId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        Tariff tariff = loadTariff(companyId, tariffId);
        tariffRepository.delete(tariff);
    }

    // ----- Components -----

    @Transactional
    public TariffResponse addComponent(UUID currentUserId, UUID companyId, UUID tariffId,
                                       CreateTariffComponentRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        Tariff tariff = loadTariff(companyId, tariffId);

        TariffComponent component = new TariffComponent();
        component.setTariff(tariff);
        component.setKind(ComponentKind.BASE);
        component.setPosition(request.position() == null ? nextPosition(tariff) : request.position());
        applyComponent(component, request.label(), request.basis(), request.unitPrice(), request.includedQuantity(),
                request.minQuantity(), request.maxQuantity(), request.minAmount(), request.maxAmount());
        tariff.getComponents().add(component);
        componentRepository.save(component);
        return TariffMapper.toResponse(tariff);
    }

    @Transactional
    public TariffResponse updateComponent(UUID currentUserId, UUID companyId, UUID tariffId, UUID componentId,
                                          UpdateTariffComponentRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        Tariff tariff = loadTariff(companyId, tariffId);
        TariffComponent component = componentRepository.findByIdAndTariffId(componentId, tariffId)
                .orElseThrow(() -> new ResourceNotFoundException("error.tariff.component.not-found"));

        if (request.position() != null) {
            component.setPosition(request.position());
        }
        if (request.label() != null) {
            component.setLabel(request.label().trim());
        }
        if (request.basis() != null) {
            component.setBasis(request.basis());
        }
        if (request.unitPrice() != null) {
            component.setUnitPrice(request.unitPrice());
        }
        if (request.includedQuantity() != null) {
            component.setIncludedQuantity(request.includedQuantity());
        }
        if (request.minQuantity() != null) {
            component.setMinQuantity(request.minQuantity());
        }
        if (request.maxQuantity() != null) {
            component.setMaxQuantity(request.maxQuantity());
        }
        if (request.minAmount() != null) {
            component.setMinAmount(request.minAmount());
        }
        if (request.maxAmount() != null) {
            component.setMaxAmount(request.maxAmount());
        }
        return TariffMapper.toResponse(tariff);
    }

    @Transactional
    public TariffResponse deleteComponent(UUID currentUserId, UUID companyId, UUID tariffId, UUID componentId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        Tariff tariff = loadTariff(companyId, tariffId);
        TariffComponent component = componentRepository.findByIdAndTariffId(componentId, tariffId)
                .orElseThrow(() -> new ResourceNotFoundException("error.tariff.component.not-found"));
        tariff.getComponents().remove(component);
        componentRepository.delete(component);
        return TariffMapper.toResponse(tariff);
    }

    // ----- Fuel surcharge policy -----

    @Transactional
    public FuelSurchargePolicyResponse upsertFuelPolicy(UUID currentUserId, UUID companyId, UUID tariffId,
                                                        UpsertFuelSurchargePolicyRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        Tariff tariff = loadTariff(companyId, tariffId);
        validatePolicy(request);

        FuelSurchargePolicy policy = tariff.getFuelSurcharge();
        if (policy == null) {
            policy = new FuelSurchargePolicy();
            policy.setTariff(tariff);
            tariff.setFuelSurcharge(policy);
        }
        policy.setEnabled(Boolean.TRUE.equals(request.enabled()));
        policy.setFuelType(request.fuelType());
        policy.setMode(request.mode());
        policy.setThresholdPrice(request.thresholdPrice());
        policy.setReferencePrice(request.referencePrice());
        policy.setDieselShareRatio(request.dieselShareRatio());
        policy.setClampAtZero(request.clampAtZero() == null || request.clampAtZero());
        policy.setSourceFilter(CompanyMapper.blankToNull(request.sourceFilter()));

        policy.getSurchargeComponents().clear();
        List<FuelSurchargeComponentRequest> requested = request.surchargeComponents();
        if (requested != null) {
            int position = 1;
            for (FuelSurchargeComponentRequest line : requested) {
                FuelSurchargeComponent component = new FuelSurchargeComponent();
                component.setPolicy(policy);
                component.setPosition(line.position() == null ? position : line.position());
                component.setLabel(line.label().trim());
                component.setBasis(line.basis());
                component.setUnitPrice(line.unitPrice());
                component.setIncludedQuantity(line.includedQuantity());
                component.setMinQuantity(line.minQuantity());
                component.setMaxQuantity(line.maxQuantity());
                component.setMinAmount(line.minAmount());
                component.setMaxAmount(line.maxAmount());
                policy.getSurchargeComponents().add(component);
                position++;
            }
        }
        policyRepository.save(policy);
        return FuelSurchargePolicyMapper.toResponse(policy);
    }

    @Transactional
    public void deleteFuelPolicy(UUID currentUserId, UUID companyId, UUID tariffId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PRICING);
        Tariff tariff = loadTariff(companyId, tariffId);
        FuelSurchargePolicy policy = tariff.getFuelSurcharge();
        if (policy != null) {
            tariff.setFuelSurcharge(null);
            policyRepository.delete(policy);
        }
    }

    // ----- Helpers -----

    private void applyComponent(TariffComponent component, String label, com.lanely.api.entity.enums.PricingBasis basis,
                                BigDecimal unitPrice, BigDecimal includedQuantity, BigDecimal minQuantity,
                                BigDecimal maxQuantity, BigDecimal minAmount, BigDecimal maxAmount) {
        component.setLabel(label.trim());
        component.setBasis(basis);
        component.setUnitPrice(unitPrice);
        component.setIncludedQuantity(includedQuantity);
        component.setMinQuantity(minQuantity);
        component.setMaxQuantity(maxQuantity);
        component.setMinAmount(minAmount);
        component.setMaxAmount(maxAmount);
    }

    private void validatePolicy(UpsertFuelSurchargePolicyRequest request) {
        if (request.mode() == FuelSurchargeMode.THRESHOLD_COMPONENTS) {
            if (request.thresholdPrice() == null) {
                throw new BadRequestException("error.fuel.policy.invalid");
            }
        } else if (request.mode() == FuelSurchargeMode.INDEXED_PERCENT) {
            if (request.referencePrice() == null || request.referencePrice().signum() <= 0
                    || request.dieselShareRatio() == null) {
                throw new BadRequestException("error.fuel.policy.invalid");
            }
        }
    }

    private Client resolveClient(UUID companyId, UUID clientId) {
        if (clientId == null || DETACH_CLIENT.equals(clientId)) {
            return null;
        }
        return clientRepository.findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.tariff.client-not-found"));
    }

    private void ensureNoOtherActiveDefault(UUID companyId, UUID selfId) {
        boolean conflict = tariffRepository.findByCompanyIdAndIsDefaultTrueAndStatus(companyId, TariffStatus.ACTIVE)
                .stream().anyMatch(other -> !other.getId().equals(selfId));
        if (conflict) {
            throw new BadRequestException("error.tariff.default-exists");
        }
    }

    private void validateValidity(LocalDate from, LocalDate until) {
        if (from != null && until != null && until.isBefore(from)) {
            throw new BadRequestException("error.tariff.invalid-validity");
        }
    }

    private int nextPosition(Tariff tariff) {
        return tariff.getComponents().stream().mapToInt(TariffComponent::getPosition).max().orElse(0) + 1;
    }

    private String currencyOrDefault(String currency) {
        String trimmed = CompanyMapper.blankToNull(currency);
        return trimmed == null ? "EUR" : trimmed.toUpperCase(java.util.Locale.ROOT);
    }

    private Tariff loadTariff(UUID companyId, UUID tariffId) {
        return tariffRepository.findByIdAndCompanyId(tariffId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.tariff.not-found"));
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new BadRequestException("error.sort.invalid", order.getProperty());
            }
        }
    }
}

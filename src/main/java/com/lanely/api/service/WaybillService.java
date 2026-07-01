package com.lanely.api.service;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.common.StatusHistoryResponse;
import com.lanely.api.dto.geo.CoordinateDto;
import com.lanely.api.dto.waybill.AssignWaybillRequest;
import com.lanely.api.dto.waybill.BulkArchiveRequest;
import com.lanely.api.dto.waybill.BulkCancelRequest;
import com.lanely.api.dto.waybill.BulkItemResult;
import com.lanely.api.dto.waybill.BulkResultResponse;
import com.lanely.api.dto.waybill.BulkStatusRequest;
import com.lanely.api.dto.waybill.ChangeParcelStatusRequest;
import com.lanely.api.dto.waybill.ChangeWaybillStatusRequest;
import com.lanely.api.dto.waybill.CreateWaybillRequest;
import com.lanely.api.dto.waybill.DockSummaryResponse;
import com.lanely.api.dto.waybill.WaybillArchivedFilter;
import com.lanely.api.dto.waybill.GoodsLineDto;
import com.lanely.api.dto.waybill.PlaceDto;
import com.lanely.api.dto.waybill.RouteInputDto;
import com.lanely.api.dto.waybill.SignatureDto;
import com.lanely.api.dto.waybill.UpdateWaybillRequest;
import com.lanely.api.dto.waybill.WaybillDateField;
import com.lanely.api.dto.waybill.WaybillPartyDto;
import com.lanely.api.dto.waybill.WaybillResponse;
import com.lanely.api.dto.waybill.WaybillSummaryResponse;
import com.lanely.api.entity.Account;
import com.lanely.api.entity.Client;
import com.lanely.api.entity.ClientAddress;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.Image;
import com.lanely.api.entity.ParcelStatusHistory;
import com.lanely.api.entity.Tour;
import com.lanely.api.entity.Waybill;
import com.lanely.api.entity.WaybillGoodsLine;
import com.lanely.api.entity.WaybillParty;
import com.lanely.api.entity.WaybillSignature;
import com.lanely.api.entity.WaybillStatusHistory;
import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.embeddable.LegalInfo;
import com.lanely.api.entity.embeddable.RouteInfo;
import com.lanely.api.entity.enums.ParcelStatus;
import com.lanely.api.entity.enums.SignatureMethod;
import com.lanely.api.entity.enums.WaybillPartyRole;
import com.lanely.api.entity.enums.WaybillScope;
import com.lanely.api.entity.enums.WaybillStatus;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.exception.ApiException;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ForbiddenAssignmentException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.exception.TransportAssignmentException;
import com.lanely.api.exception.WaybillNotEditableException;
import com.lanely.api.exception.WaybillReferenceTakenException;
import com.lanely.api.mapper.CompanyMapper;
import com.lanely.api.mapper.StatusHistoryMapper;
import com.lanely.api.mapper.WaybillMapper;
import com.lanely.api.repository.AccountRepository;
import com.lanely.api.repository.ClientAddressRepository;
import com.lanely.api.repository.ClientRepository;
import com.lanely.api.repository.ParcelStatusHistoryRepository;
import com.lanely.api.repository.TourRepository;
import com.lanely.api.repository.WaybillRepository;
import com.lanely.api.repository.WaybillStatusHistoryRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class WaybillService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("reference", "status", "createdAt", "updatedAt", "deliveryPlannedAt", "pickupPlannedAt", "dockEnteredAt");

    private static final Map<String, String> SORT_PROPERTY_ALIASES =
            Map.of("pickupPlannedAt", "takingOverPlannedAt");

    static final int MAX_BULK_IDS = 200;

    private static final String CODE_WAYBILL_NOT_FOUND = "WAYBILL_NOT_FOUND";
    private static final String CODE_INVALID_TRANSITION = "INVALID_TRANSITION";
    private static final String CODE_FAILURE_REASON_REQUIRED = "FAILURE_REASON_REQUIRED";

    private static final Map<String, String> ITEM_ERROR_CODES = Map.of(
            "error.waybill.not-found", CODE_WAYBILL_NOT_FOUND,
            "error.waybill.invalid-status-transition", CODE_INVALID_TRANSITION,
            "error.waybill.failure-reason-required", CODE_FAILURE_REASON_REQUIRED);

    private final MessageSource messageSource;
    private final CompanyService companyService;
    private final WaybillRepository waybillRepository;
    private final WaybillStatusHistoryRepository waybillStatusHistoryRepository;
    private final ParcelStatusHistoryRepository parcelStatusHistoryRepository;
    private final TourRepository tourRepository;
    private final ClientRepository clientRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final AccountRepository accountRepository;
    private final AssigneeResolver assigneeResolver;
    private final GeocodingService geocodingService;
    private final RoutingService routingService;
    private final ImageService imageService;

    public WaybillService(MessageSource messageSource, CompanyService companyService, WaybillRepository waybillRepository,
                          WaybillStatusHistoryRepository waybillStatusHistoryRepository,
                          ParcelStatusHistoryRepository parcelStatusHistoryRepository,
                          TourRepository tourRepository, ClientRepository clientRepository,
                          ClientAddressRepository clientAddressRepository, AccountRepository accountRepository,
                          AssigneeResolver assigneeResolver,
                          GeocodingService geocodingService, RoutingService routingService,
                          ImageService imageService) {
        this.messageSource = messageSource;
        this.companyService = companyService;
        this.waybillRepository = waybillRepository;
        this.waybillStatusHistoryRepository = waybillStatusHistoryRepository;
        this.parcelStatusHistoryRepository = parcelStatusHistoryRepository;
        this.tourRepository = tourRepository;
        this.clientRepository = clientRepository;
        this.clientAddressRepository = clientAddressRepository;
        this.accountRepository = accountRepository;
        this.assigneeResolver = assigneeResolver;
        this.geocodingService = geocodingService;
        this.routingService = routingService;
        this.imageService = imageService;
    }

    // ----- Web (User, MANAGE_TRANSPORTS) -----

    @Transactional
    public WaybillResponse createWaybill(UUID currentUserId, UUID companyId, CreateWaybillRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Company company = membership.getCompany();

        Waybill waybill = new Waybill();
        waybill.setCompany(company);
        waybill.setClient(requireClient(companyId, request.clientId()));
        waybill.setReference(resolveNewReference(companyId, request.reference()));
        waybill.setScope(request.scope() == null ? WaybillScope.NATIONAL : request.scope());
        waybill.setStatus(WaybillStatus.DRAFT);
        recordStatusHistory(waybill, null, WaybillStatus.DRAFT, membership.getUser(), null, null, null);

        addParty(waybill, WaybillPartyRole.SHIPPER, request.shipper(), companyId);
        addParty(waybill, WaybillPartyRole.CONSIGNEE, request.consignee(), companyId);

        applyTakingOver(waybill, request.placeOfTakingOver(), companyId);
        applyDelivery(waybill, request.placeOfDelivery(), companyId);
        replaceGoodsLines(waybill, request.goodsLines());
        applyCmrFields(waybill, request.attachedDocuments(), request.senderInstructions(),
                request.carriageChargesAmount(), request.carriageChargesCurrency(), request.reservationsAndObservations());
        waybill.setNotes(CompanyMapper.blankToNull(request.notes()));

        if (request.tourId() != null) {
            attachToTour(waybill, companyId, request.tourId(), null);
        }
        if (request.assignedAccountId() != null) {
            waybill.setAssignedAccount(assigneeResolver.resolveAssignee(companyId, request.assignedAccountId()));
        }

        waybill.setRoute(computeWaybillRoute(waybill, request.route()));
        waybillRepository.save(waybill);
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional(readOnly = true)
    public PageResponse<WaybillSummaryResponse> listWaybills(UUID currentUserId, UUID companyId,
                                                             List<WaybillStatus> statuses, String archived,
                                                             String q, UUID tourId, UUID assignedAccountId, UUID clientId,
                                                             WaybillDateField dateField, Instant dateFrom, Instant dateTo,
                                                             Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        validateSort(pageable.getSort());

        boolean hasStatus = statuses != null && !statuses.isEmpty();
        Collection<WaybillStatus> statusFilter = hasStatus ? statuses : List.of(WaybillStatus.DRAFT);
        Boolean archivedFilter = WaybillArchivedFilter.parse(archived).toArchivedFlag();

        DateWindows windows = resolveDateWindows(dateField, dateFrom, dateTo);

        Page<WaybillSummaryResponse> page = waybillRepository
                .search(companyId, hasStatus, statusFilter, archivedFilter, tourId, assignedAccountId, clientId,
                        windows.pickupFrom(), windows.pickupTo(), windows.deliveryFrom(), windows.deliveryTo(),
                        windows.dockFrom(), windows.dockTo(),
                        searchPattern(q), translateSort(pageable))
                .map(WaybillMapper::toSummary);
        return PageResponse.of(page);
    }

    private DateWindows resolveDateWindows(WaybillDateField dateField, Instant dateFrom, Instant dateTo) {
        if (dateFrom == null && dateTo == null) {
            return DateWindows.EMPTY;
        }
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BadRequestException("error.waybill.date-range-invalid");
        }
        WaybillDateField field = dateField == null ? WaybillDateField.PICKUP : dateField;
        return switch (field) {
            case PICKUP -> new DateWindows(dateFrom, dateTo, null, null, null, null);
            case DELIVERY -> new DateWindows(null, null, dateFrom, dateTo, null, null);
            case DOCK -> new DateWindows(null, null, null, null, dateFrom, dateTo);
        };
    }

    private record DateWindows(Instant pickupFrom, Instant pickupTo, Instant deliveryFrom, Instant deliveryTo,
                               Instant dockFrom, Instant dockTo) {
        private static final DateWindows EMPTY = new DateWindows(null, null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<WaybillSummaryResponse> listDock(UUID currentUserId, UUID companyId, String q, UUID clientId,
                                                         Instant dockFrom, Instant dockTo, Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        validateSort(pageable.getSort());
        if (dockFrom != null && dockTo != null && dockFrom.isAfter(dockTo)) {
            throw new BadRequestException("error.waybill.date-range-invalid");
        }
        Page<WaybillSummaryResponse> page = waybillRepository
                .searchAtDock(companyId, clientId, dockFrom, dockTo, searchPattern(q), translateSort(pageable))
                .map(WaybillMapper::toSummary);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public DockSummaryResponse dockSummary(UUID currentUserId, UUID companyId) {
        companyService.requireMember(companyId, currentUserId);
        long waybillCount = waybillRepository.countAtDock(companyId);
        List<Object[]> rows = waybillRepository.sumGoodsAtDock(companyId);
        Object[] totals = rows.isEmpty() ? new Object[]{0L, BigDecimal.ZERO, BigDecimal.ZERO} : rows.get(0);
        long totalPackages = totals[0] == null ? 0L : ((Number) totals[0]).longValue();
        return new DockSummaryResponse(waybillCount, totalPackages, toBigDecimal(totals[1]), toBigDecimal(totals[2]));
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }

    @Transactional(readOnly = true)
    public WaybillResponse getWaybill(UUID currentUserId, UUID companyId, UUID waybillId) {
        companyService.requireMember(companyId, currentUserId);
        return WaybillMapper.toResponse(loadWaybill(companyId, waybillId));
    }

    @Transactional(readOnly = true)
    public PageResponse<StatusHistoryResponse> getStatusHistory(UUID currentUserId, UUID companyId, UUID waybillId,
                                                                Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        loadWaybill(companyId, waybillId);
        return statusHistoryPage(waybillId, pageable);
    }

    @Transactional
    public WaybillResponse updateWaybill(UUID currentUserId, UUID companyId, UUID waybillId,
                                         UpdateWaybillRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Waybill waybill = loadWaybill(companyId, waybillId);
        requireEditable(waybill);

        if (request.reference() != null) {
            String reference = request.reference().trim();
            if (!reference.equals(waybill.getReference())
                    && waybillRepository.existsByCompanyIdAndReference(companyId, reference)) {
                throw new WaybillReferenceTakenException("error.waybill.reference-taken");
            }
            waybill.setReference(reference);
        }
        if (request.scope() != null) {
            waybill.setScope(request.scope());
        }
        if (request.clientId() != null) {
            waybill.setClient(requireClient(companyId, request.clientId()));
        }
        if (request.shipper() != null) {
            replaceParty(waybill, WaybillPartyRole.SHIPPER, request.shipper(), companyId);
        }
        if (request.consignee() != null) {
            replaceParty(waybill, WaybillPartyRole.CONSIGNEE, request.consignee(), companyId);
        }
        if (request.placeOfTakingOver() != null) {
            applyTakingOver(waybill, request.placeOfTakingOver(), companyId);
        }
        if (request.placeOfDelivery() != null) {
            applyDelivery(waybill, request.placeOfDelivery(), companyId);
        }
        if (request.goodsLines() != null) {
            replaceGoodsLines(waybill, request.goodsLines());
        }
        applyCmrFields(waybill, request.attachedDocuments(), request.senderInstructions(),
                request.carriageChargesAmount(), request.carriageChargesCurrency(), request.reservationsAndObservations());
        if (request.notes() != null) {
            waybill.setNotes(CompanyMapper.blankToNull(request.notes()));
        }

        waybill.setRoute(computeWaybillRoute(waybill, request.route()));
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional
    public WaybillResponse changeStatus(UUID currentUserId, UUID companyId, UUID waybillId,
                                        ChangeWaybillStatusRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Waybill waybill = loadWaybill(companyId, waybillId);
        applyStatusChange(waybill, request, membership.getUser());
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional
    public WaybillResponse assign(UUID currentUserId, UUID companyId, UUID waybillId, AssignWaybillRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Waybill waybill = loadWaybill(companyId, waybillId);

        if (request.tourId() == null) {
            waybill.setTour(null);
            waybill.setPositionInTour(null);
        } else {
            attachToTour(waybill, companyId, request.tourId(), request.positionInTour());
        }
        waybill.setAssignedAccount(request.assignedAccountId() == null
                ? null : assigneeResolver.resolveAssignee(companyId, request.assignedAccountId()));
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional
    public WaybillResponse addSignature(UUID currentUserId, UUID companyId, UUID waybillId, SignatureDto request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Waybill waybill = loadWaybill(companyId, waybillId);
        addSignatureInternal(waybill, request);
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional
    public WaybillResponse cancel(UUID currentUserId, UUID companyId, UUID waybillId) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Waybill waybill = loadWaybill(companyId, waybillId);
        transitionTo(waybill, WaybillStatus.CANCELLED, membership.getUser(), null, null, null);
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional
    public WaybillResponse archive(UUID currentUserId, UUID companyId, UUID waybillId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Waybill waybill = loadWaybill(companyId, waybillId);
        setArchived(waybill, true);
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional
    public WaybillResponse unarchive(UUID currentUserId, UUID companyId, UUID waybillId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Waybill waybill = loadWaybill(companyId, waybillId);
        setArchived(waybill, false);
        return WaybillMapper.toResponse(waybill);
    }

    // ----- Bulk (web, MANAGE_TRANSPORTS) -----

    @Transactional
    public BulkResultResponse bulkChangeStatus(UUID currentUserId, UUID companyId, BulkStatusRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        List<UUID> ids = requireBulkIds(request.ids());
        ChangeWaybillStatusRequest perItem = new ChangeWaybillStatusRequest(
                request.status(), request.failureReason(), request.note(), null, null);
        return processBulk(ids, id -> applyStatusChange(loadWaybill(companyId, id), perItem, membership.getUser()));
    }

    @Transactional
    public BulkResultResponse bulkArchive(UUID currentUserId, UUID companyId, BulkArchiveRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        List<UUID> ids = requireBulkIds(request.ids());
        return processBulk(ids, id -> setArchived(loadWaybill(companyId, id), request.archived()));
    }

    @Transactional
    public BulkResultResponse bulkCancel(UUID currentUserId, UUID companyId, BulkCancelRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        List<UUID> ids = requireBulkIds(request.ids());
        return processBulk(ids, id -> transitionTo(loadWaybill(companyId, id), WaybillStatus.CANCELLED,
                membership.getUser(), request.note(), null, null));
    }

    @Transactional
    public WaybillResponse changeParcelStatus(UUID currentUserId, UUID companyId, UUID waybillId, UUID parcelId,
                                              ChangeParcelStatusRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Waybill waybill = loadWaybill(companyId, waybillId);
        applyParcelStatusChange(requireParcel(waybill, parcelId), request, membership.getUser());
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional(readOnly = true)
    public PageResponse<StatusHistoryResponse> getParcelStatusHistory(UUID currentUserId, UUID companyId, UUID waybillId,
                                                                      UUID parcelId, Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        requireParcel(loadWaybill(companyId, waybillId), parcelId);
        return parcelStatusHistoryPage(parcelId, pageable);
    }

    // ----- Assignee surface (mobile Profile via /profile, web User via /companies/{id}/assignments) -----

    @Transactional(readOnly = true)
    public PageResponse<WaybillSummaryResponse> listForAssignee(UUID companyId, UUID accountId, WaybillStatus status,
                                                                WaybillDateField dateField, Instant dateFrom, Instant dateTo,
                                                                Pageable pageable) {
        validateSort(pageable.getSort());
        DateWindows windows = resolveDateWindows(dateField, dateFrom, dateTo);
        Page<WaybillSummaryResponse> page = waybillRepository
                .findAssignedToAccount(companyId, accountId, status,
                        windows.pickupFrom(), windows.pickupTo(), windows.deliveryFrom(), windows.deliveryTo(),
                        windows.dockFrom(), windows.dockTo(), pageable)
                .map(WaybillMapper::toSummary);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public WaybillResponse getForAssignee(UUID companyId, UUID accountId, UUID waybillId) {
        return WaybillMapper.toResponse(loadAssignedWaybill(companyId, accountId, waybillId));
    }

    @Transactional(readOnly = true)
    public PageResponse<StatusHistoryResponse> getStatusHistoryForAssignee(UUID companyId, UUID accountId, UUID waybillId,
                                                                           Pageable pageable) {
        loadAssignedWaybill(companyId, accountId, waybillId);
        return statusHistoryPage(waybillId, pageable);
    }

    @Transactional
    public WaybillResponse changeParcelStatusForAssignee(UUID companyId, UUID accountId, UUID waybillId, UUID parcelId,
                                                         ChangeParcelStatusRequest request) {
        Waybill waybill = loadAssignedWaybill(companyId, accountId, waybillId);
        Account actor = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("error.account.not-found"));
        applyParcelStatusChange(requireParcel(waybill, parcelId), request, actor);
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional(readOnly = true)
    public PageResponse<StatusHistoryResponse> getParcelStatusHistoryForAssignee(UUID companyId, UUID accountId,
                                                                                 UUID waybillId, UUID parcelId,
                                                                                 Pageable pageable) {
        requireParcel(loadAssignedWaybill(companyId, accountId, waybillId), parcelId);
        return parcelStatusHistoryPage(parcelId, pageable);
    }

    @Transactional
    public WaybillResponse changeStatusForAssignee(UUID companyId, UUID accountId, UUID waybillId,
                                                   ChangeWaybillStatusRequest request) {
        Waybill waybill = loadAssignedWaybill(companyId, accountId, waybillId);
        Account actor = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("error.account.not-found"));
        applyStatusChange(waybill, request, actor);
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional
    public WaybillResponse addSignatureForAssignee(UUID companyId, UUID accountId, UUID waybillId, SignatureDto request) {
        Waybill waybill = loadAssignedWaybill(companyId, accountId, waybillId);
        addSignatureInternal(waybill, request);
        return WaybillMapper.toResponse(waybill);
    }

    @Transactional
    public WaybillResponse addProofForAssignee(UUID companyId, UUID accountId, UUID waybillId, MultipartFile file) {
        Waybill waybill = loadAssignedWaybill(companyId, accountId, waybillId);
        Image previous = waybill.getProofOfDeliveryImage();
        waybill.setProofOfDeliveryImage(imageService.upload(file));
        if (previous != null) {
            imageService.delete(previous);
        }
        return WaybillMapper.toResponse(waybill);
    }

    // ----- Helpers -----

    private void addSignatureInternal(Waybill waybill, SignatureDto request) {
        if (request.role() == WaybillPartyRole.CARRIER) {
            throw new BadRequestException("error.waybill.carrier-not-supported");
        }
        WaybillSignature signature = new WaybillSignature();
        signature.setWaybill(waybill);
        signature.setRole(request.role());
        signature.setSignerName(request.signerName().trim());
        signature.setPlace(CompanyMapper.blankToNull(request.place()));
        signature.setMethod(request.method() == null ? SignatureMethod.CLICKWRAP : request.method());
        signature.setSignedAt(Instant.now());
        waybill.getSignatures().add(signature);
    }

    private void applyStatusChange(Waybill waybill, ChangeWaybillStatusRequest request, Account actor) {
        WaybillStatus target = request.status();
        if (target == WaybillStatus.FAILED && CompanyMapper.blankToNull(request.failureReason()) == null) {
            throw new BadRequestException("error.waybill.failure-reason-required");
        }
        WaybillStatus previous = waybill.getStatus();
        transitionTo(waybill, target, actor, request.note(), request.latitude(), request.longitude());
        Instant now = Instant.now();
        if (target == WaybillStatus.COLLECTED && waybill.getTakingOverActualAt() == null) {
            waybill.setTakingOverActualAt(now);
        }
        if (target == WaybillStatus.DELIVERED) {
            waybill.setDeliveryActualAt(now);
        }
        if (target == WaybillStatus.FAILED) {
            waybill.setFailureReason(CompanyMapper.blankToNull(request.failureReason()));
        }
        if (target == WaybillStatus.AT_DOCK) {
            waybill.setDockEnteredAt(now);
            waybill.setDockExitedAt(null);
        } else if (previous == WaybillStatus.AT_DOCK) {
            waybill.setDockExitedAt(now);
        }
    }

    private void setArchived(Waybill waybill, boolean archived) {
        if (waybill.isArchived() == archived) {
            return;
        }
        waybill.setArchived(archived);
        waybill.setArchivedAt(archived ? Instant.now() : null);
    }

    private List<UUID> requireBulkIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("error.waybill.bulk.empty");
        }
        if (ids.size() > MAX_BULK_IDS) {
            throw new BadRequestException("error.waybill.bulk.too-many", MAX_BULK_IDS);
        }
        List<UUID> deduped = ids.stream().filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .stream().toList();
        if (deduped.isEmpty()) {
            throw new BadRequestException("error.waybill.bulk.empty");
        }
        return deduped;
    }

    private BulkResultResponse processBulk(List<UUID> ids, java.util.function.Consumer<UUID> action) {
        List<BulkItemResult> results = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            try {
                action.accept(id);
                results.add(BulkItemResult.ok(id));
            } catch (ApiException ex) {
                results.add(toItemError(id, ex));
            }
        }
        return BulkResultResponse.of(results);
    }

    private BulkItemResult toItemError(UUID id, ApiException ex) {
        String code = ITEM_ERROR_CODES.getOrDefault(ex.getMessageKey(), "ERROR");
        Object[] args = (ex.getArgs() == null || ex.getArgs().length == 0) ? null : ex.getArgs();
        String message = messageSource.getMessage(ex.getMessageKey(), args, LocaleContextHolder.getLocale());
        return BulkItemResult.error(id, code, message);
    }

    private void transitionTo(Waybill waybill, WaybillStatus target, Account actor, String note,
                              Double latitude, Double longitude) {
        WaybillStatus current = waybill.getStatus();
        if (current == target) {
            return;
        }
        waybill.setStatus(target);
        recordStatusHistory(waybill, current, target, actor, note, latitude, longitude);
    }

    private void recordStatusHistory(Waybill waybill, WaybillStatus fromStatus, WaybillStatus toStatus,
                                     Account actor, String note, Double latitude, Double longitude) {
        WaybillStatusHistory entry = new WaybillStatusHistory();
        entry.setWaybill(waybill);
        entry.setFromStatus(fromStatus);
        entry.setToStatus(toStatus);
        entry.setChangedBy(actor);
        entry.setChangedByName(StatusHistorySupport.actorName(actor));
        entry.setActorType(StatusHistorySupport.actorType(actor));
        entry.setNote(CompanyMapper.blankToNull(note));
        entry.setLatitude(latitude);
        entry.setLongitude(longitude);
        entry.setChangedAt(Instant.now());
        waybill.getStatusHistory().add(entry);
    }

    private PageResponse<StatusHistoryResponse> statusHistoryPage(UUID waybillId, Pageable pageable) {
        Page<StatusHistoryResponse> page = waybillStatusHistoryRepository
                .findByWaybillIdOrderByChangedAtDesc(waybillId, pageable)
                .map(StatusHistoryMapper::toResponse);
        return PageResponse.of(page);
    }

    private WaybillGoodsLine requireParcel(Waybill waybill, UUID parcelId) {
        return waybill.getGoodsLines().stream()
                .filter(line -> line.getId().equals(parcelId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("error.parcel.not-found"));
    }

    private void applyParcelStatusChange(WaybillGoodsLine parcel, ChangeParcelStatusRequest request, Account actor) {
        ParcelStatus current = parcel.getStatus();
        ParcelStatus target = request.status();
        if (current == target) {
            return;
        }
        parcel.setStatus(target);
        Instant now = Instant.now();
        if (target == ParcelStatus.AT_DOCK) {
            parcel.setDockEnteredAt(now);
            parcel.setDockExitedAt(null);
        } else if (current == ParcelStatus.AT_DOCK) {
            parcel.setDockExitedAt(now);
        }
        ParcelStatusHistory entry = new ParcelStatusHistory();
        entry.setParcel(parcel);
        entry.setFromStatus(current);
        entry.setToStatus(target);
        entry.setChangedBy(actor);
        entry.setChangedByName(StatusHistorySupport.actorName(actor));
        entry.setActorType(StatusHistorySupport.actorType(actor));
        entry.setNote(CompanyMapper.blankToNull(request.note()));
        entry.setLatitude(request.latitude());
        entry.setLongitude(request.longitude());
        entry.setChangedAt(Instant.now());
        parcel.getStatusHistory().add(entry);
    }

    private PageResponse<StatusHistoryResponse> parcelStatusHistoryPage(UUID parcelId, Pageable pageable) {
        Page<StatusHistoryResponse> page = parcelStatusHistoryRepository
                .findByParcelIdOrderByChangedAtDesc(parcelId, pageable)
                .map(StatusHistoryMapper::toResponse);
        return PageResponse.of(page);
    }

    private void requireEditable(Waybill waybill) {
        if (waybill.getStatus() != WaybillStatus.DRAFT && waybill.getStatus() != WaybillStatus.ISSUED) {
            throw new WaybillNotEditableException("error.waybill.not-editable");
        }
    }

    private void attachToTour(Waybill waybill, UUID companyId, UUID tourId, Integer position) {
        Tour tour = tourRepository.findByIdAndCompanyId(tourId, companyId)
                .orElseThrow(() -> new TransportAssignmentException("error.tour.other-company"));
        waybill.setTour(tour);
        if (position != null) {
            waybill.setPositionInTour(position);
        } else if (waybill.getPositionInTour() == null) {
            waybill.setPositionInTour(waybillRepository.findByTourIdOrderByPositionInTourAsc(tourId).size());
        }
    }

    private void addParty(Waybill waybill, WaybillPartyRole role, WaybillPartyDto dto, UUID companyId) {
        if (dto == null) {
            return;
        }
        WaybillParty party = new WaybillParty();
        party.setWaybill(waybill);
        party.setRole(role);
        populateParty(party, dto, companyId);
        waybill.getParties().add(party);
    }

    private void replaceParty(Waybill waybill, WaybillPartyRole role, WaybillPartyDto dto, UUID companyId) {
        waybill.getParties().removeIf(p -> p.getRole() == role);
        addParty(waybill, role, dto, companyId);
    }

    private void populateParty(WaybillParty party, WaybillPartyDto dto, UUID companyId) {
        ClientAddress clientAddress = resolveClientAddress(companyId, dto.clientId(), dto.clientAddressId());
        Client client = clientAddress != null ? clientAddress.getClient() : resolveClient(companyId, dto.clientId());

        party.setName(dto.name().trim());
        party.setClientId(dto.clientId());
        party.setClientAddressId(dto.clientAddressId());

        Address address;
        if (dto.address() != null) {
            address = CompanyMapper.toAddress(dto.address());
        } else if (clientAddress != null) {
            address = copyAddress(clientAddress.getAddress());
        } else {
            address = new Address();
        }
        party.setAddress(address);

        GeoPoint location = resolveLocation(dto.location(), clientAddress, address);
        party.setLocation(location);

        party.setContactName(firstNonBlank(dto.contactName(), clientAddress == null ? null : clientAddress.getContactName()));
        party.setContactPhone(firstNonBlank(dto.contactPhone(), clientAddress == null ? null : clientAddress.getContactPhone()));
        party.setContactEmail(firstNonBlank(dto.contactEmail(), clientAddress == null ? null : clientAddress.getContactEmail()));

        if (dto.legalInfo() != null) {
            party.setLegalInfo(CompanyMapper.toLegalInfo(dto.legalInfo()));
        } else if (client != null) {
            party.setLegalInfo(copyLegalInfo(client.getLegalInfo()));
        }
    }

    private GeoPoint resolveLocation(CoordinateDto dtoLocation, ClientAddress clientAddress, Address address) {
        if (dtoLocation != null && dtoLocation.isComplete()) {
            return new GeoPoint(dtoLocation.latitude(), dtoLocation.longitude());
        }
        if (clientAddress != null && clientAddress.getLatitude() != null && clientAddress.getLongitude() != null) {
            return new GeoPoint(clientAddress.getLatitude(), clientAddress.getLongitude());
        }
        return safeGeocode(address);
    }

    private ClientAddress resolveClientAddress(UUID companyId, UUID clientId, UUID clientAddressId) {
        if (clientId == null || clientAddressId == null) {
            return null;
        }
        ClientAddress address = clientAddressRepository.findByIdAndClientId(clientAddressId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("error.client.address.not-found"));
        if (!address.getClient().getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("error.client.address.not-found");
        }
        return address;
    }

    private Client resolveClient(UUID companyId, UUID clientId) {
        if (clientId == null) {
            return null;
        }
        return clientRepository.findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.client.not-found"));
    }

    private Client requireClient(UUID companyId, UUID clientId) {
        if (clientId == null) {
            throw new ResourceNotFoundException("error.client.not-found");
        }
        return resolveClient(companyId, clientId);
    }

    private void applyTakingOver(Waybill waybill, PlaceDto place, UUID companyId) {
        if (place == null) {
            return;
        }
        ResolvedPlace resolved = resolvePlace(companyId, place,
                waybill.getPlaceOfTakingOver(), waybill.getTakingOverLocation());
        waybill.setPlaceOfTakingOver(resolved.address());
        waybill.setTakingOverLocation(resolved.location());
        waybill.setPickupClientId(place.clientId());
        waybill.setPickupClientAddressId(place.clientAddressId());
        waybill.setTakingOverPlannedAt(place.plannedAt());
    }

    private void applyDelivery(Waybill waybill, PlaceDto place, UUID companyId) {
        if (place == null) {
            return;
        }
        ResolvedPlace resolved = resolvePlace(companyId, place,
                waybill.getPlaceOfDelivery(), waybill.getDeliveryLocation());
        waybill.setPlaceOfDelivery(resolved.address());
        waybill.setDeliveryLocation(resolved.location());
        waybill.setDeliveryClientId(place.clientId());
        waybill.setDeliveryClientAddressId(place.clientAddressId());
        waybill.setDeliveryPlannedAt(place.plannedAt());
    }

    private ResolvedPlace resolvePlace(UUID companyId, PlaceDto place, Address current, GeoPoint currentLocation) {
        ClientAddress clientAddress = resolveClientAddress(companyId, place.clientId(), place.clientAddressId());
        if (clientAddress == null && place.clientId() != null) {
            resolveClient(companyId, place.clientId());
        }

        boolean customAddress = place.address() != null;
        Address address;
        if (customAddress) {
            address = CompanyMapper.toAddress(place.address());
        } else if (clientAddress != null) {
            address = copyAddress(clientAddress.getAddress());
        } else {
            address = current != null ? current : new Address();
        }

        GeoPoint location = resolvePlaceLocation(place, clientAddress, customAddress, currentLocation, address);
        return new ResolvedPlace(address, location);
    }

    private GeoPoint resolvePlaceLocation(PlaceDto place, ClientAddress clientAddress, boolean customAddress,
                                          GeoPoint currentLocation, Address address) {
        if (place.location() != null && place.location().isComplete()) {
            return new GeoPoint(place.location().latitude(), place.location().longitude());
        }
        if (!customAddress && clientAddress != null
                && clientAddress.getLatitude() != null && clientAddress.getLongitude() != null) {
            return new GeoPoint(clientAddress.getLatitude(), clientAddress.getLongitude());
        }
        if (!customAddress && clientAddress == null && currentLocation != null && currentLocation.isComplete()) {
            return currentLocation;
        }
        return geocodingService.geocodeRequired(address);
    }

    private record ResolvedPlace(Address address, GeoPoint location) {
    }

    private void applyCmrFields(Waybill waybill, String attachedDocuments, String senderInstructions,
                                java.math.BigDecimal amount, String currency, String reservations) {
        if (attachedDocuments != null) {
            waybill.setAttachedDocuments(CompanyMapper.blankToNull(attachedDocuments));
        }
        if (senderInstructions != null) {
            waybill.setSenderInstructions(CompanyMapper.blankToNull(senderInstructions));
        }
        if (amount != null) {
            waybill.setCarriageChargesAmount(amount);
        }
        if (currency != null) {
            waybill.setCarriageChargesCurrency(CompanyMapper.blankToNull(currency));
        }
        if (reservations != null) {
            waybill.setReservationsAndObservations(CompanyMapper.blankToNull(reservations));
        }
    }

    private void replaceGoodsLines(Waybill waybill, List<GoodsLineDto> lines) {
        if (lines == null) {
            return;
        }
        waybill.getGoodsLines().clear();
        int position = 0;
        for (GoodsLineDto dto : lines) {
            WaybillGoodsLine line = new WaybillGoodsLine();
            line.setWaybill(waybill);
            line.setPosition(position++);
            line.setDescription(dto.description().trim());
            line.setPackagingType(CompanyMapper.blankToNull(dto.packagingType()));
            line.setNumberOfPackages(dto.numberOfPackages());
            line.setMarksAndNumbers(CompanyMapper.blankToNull(dto.marksAndNumbers()));
            line.setGrossWeightKg(dto.grossWeightKg());
            line.setVolumeM3(dto.volumeM3());
            line.setLengthCm(dto.lengthCm());
            line.setWidthCm(dto.widthCm());
            line.setHeightCm(dto.heightCm());
            line.setDangerousGoods(dto.dangerousGoods() != null && dto.dangerousGoods());
            line.setUnNumber(CompanyMapper.blankToNull(dto.unNumber()));
            waybill.getGoodsLines().add(line);
        }
    }

    private RouteInfo computeWaybillRoute(Waybill waybill, RouteInputDto provided) {
        if (provided != null && provided.hasAnyValue()) {
            return suppliedRoute(provided);
        }
        GeoPoint depot = companyLocation(waybill.getCompany());
        List<GeoPoint> points = new ArrayList<>();
        points.add(depot);
        points.add(waybill.getTakingOverLocation());
        points.add(waybill.getDeliveryLocation());
        points.add(depot);
        try {
            return routingService.routeOrdered(points);
        } catch (ApiException ex) {
            return new RouteInfo();
        }
    }

    private RouteInfo suppliedRoute(RouteInputDto provided) {
        RouteInfo info = new RouteInfo();
        info.setDistanceMeters(provided.distanceMeters());
        info.setDurationSeconds(provided.durationSeconds());
        info.setGeometryPolyline(CompanyMapper.blankToNull(provided.geometryPolyline()));
        info.setComputedAt(Instant.now());
        return info;
    }

    private GeoPoint companyLocation(Company company) {
        if (company == null) {
            return null;
        }
        GeoPoint coordinate = company.getDepositCoordinate();
        if (coordinate != null && coordinate.isComplete()) {
            return coordinate;
        }
        return safeGeocode(company.getDepositAddress());
    }

    private GeoPoint safeGeocode(Address address) {
        try {
            return geocodingService.geocodeBest(address);
        } catch (ApiException ex) {
            return new GeoPoint();
        }
    }

    private Waybill loadWaybill(UUID companyId, UUID waybillId) {
        return waybillRepository.findByIdAndCompanyId(waybillId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.waybill.not-found"));
    }

    private Waybill loadAssignedWaybill(UUID companyId, UUID accountId, UUID waybillId) {
        return waybillRepository.findAssignedToAccount(waybillId, companyId, accountId)
                .orElseThrow(() -> new ForbiddenAssignmentException("error.waybill.not-assigned"));
    }

    private String resolveNewReference(UUID companyId, String requested) {
        String reference = CompanyMapper.blankToNull(requested);
        if (reference != null) {
            if (waybillRepository.existsByCompanyIdAndReference(companyId, reference)) {
                throw new WaybillReferenceTakenException("error.waybill.reference-taken");
            }
            return reference;
        }
        long next = waybillRepository.countByCompanyId(companyId) + 1;
        String generated;
        do {
            generated = String.format("WBL-%04d", next++);
        } while (waybillRepository.existsByCompanyIdAndReference(companyId, generated));
        return generated;
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new BadRequestException("error.sort.invalid", order.getProperty());
            }
        }
    }

    private Pageable translateSort(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort.isEmpty() || sort.stream().noneMatch(order -> SORT_PROPERTY_ALIASES.containsKey(order.getProperty()))) {
            return pageable;
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            String property = SORT_PROPERTY_ALIASES.getOrDefault(order.getProperty(), order.getProperty());
            orders.add(new Sort.Order(order.getDirection(), property, order.getNullHandling()));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    private String searchPattern(String q) {
        String trimmed = CompanyMapper.blankToNull(q);
        return trimmed == null ? "%" : "%" + trimmed.toLowerCase(Locale.ROOT) + "%";
    }

    private Address copyAddress(Address source) {
        Address copy = new Address();
        if (source != null) {
            copy.setLine1(source.getLine1());
            copy.setLine2(source.getLine2());
            copy.setPostalCode(source.getPostalCode());
            copy.setCity(source.getCity());
            copy.setState(source.getState());
            copy.setCountry(source.getCountry());
        }
        return copy;
    }

    private LegalInfo copyLegalInfo(LegalInfo source) {
        LegalInfo copy = new LegalInfo();
        if (source != null) {
            copy.setLegalName(source.getLegalName());
            copy.setRegistrationNumber(source.getRegistrationNumber());
            copy.setVatNumber(source.getVatNumber());
            copy.setLegalForm(source.getLegalForm());
        }
        return copy;
    }

    private String firstNonBlank(String primary, String fallback) {
        String value = CompanyMapper.blankToNull(primary);
        return value != null ? value : CompanyMapper.blankToNull(fallback);
    }
}

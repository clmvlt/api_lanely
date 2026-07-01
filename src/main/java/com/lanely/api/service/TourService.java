package com.lanely.api.service;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.common.StatusHistoryResponse;
import com.lanely.api.dto.geo.SkippedVisitDto;
import com.lanely.api.dto.tour.AssignTourRequest;
import com.lanely.api.dto.tour.ChangeTourStatusRequest;
import com.lanely.api.dto.tour.CreateTourRequest;
import com.lanely.api.dto.tour.RoutePreviewRequest;
import com.lanely.api.dto.tour.RoutePreviewResponse;
import com.lanely.api.dto.tour.SetTourWaybillsRequest;
import com.lanely.api.dto.tour.TourOptimizeResponse;
import com.lanely.api.dto.tour.TourResponse;
import com.lanely.api.dto.tour.TourSummaryResponse;
import com.lanely.api.dto.tour.UpdateTourRequest;
import com.lanely.api.entity.Account;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.Tour;
import com.lanely.api.entity.TourStatusHistory;
import com.lanely.api.entity.Vehicle;
import com.lanely.api.entity.Waybill;
import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.embeddable.RouteInfo;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.entity.enums.TourStatus;
import com.lanely.api.exception.ApiException;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ForbiddenAssignmentException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.exception.TourReferenceTakenException;
import com.lanely.api.exception.TransportAssignmentException;
import com.lanely.api.integration.ors.OptimizationClient;
import com.lanely.api.integration.ors.dto.OrsCoordinate;
import com.lanely.api.integration.ors.dto.OrsOptimizeRequest;
import com.lanely.api.integration.ors.dto.OrsOptimizeResponse;
import com.lanely.api.integration.ors.dto.OrsRoute;
import com.lanely.api.integration.ors.dto.OrsStop;
import com.lanely.api.integration.ors.dto.OrsVisit;
import com.lanely.api.mapper.CompanyMapper;
import com.lanely.api.mapper.GeoMapper;
import com.lanely.api.mapper.StatusHistoryMapper;
import com.lanely.api.mapper.TourMapper;
import com.lanely.api.repository.AccountRepository;
import com.lanely.api.repository.TourRepository;
import com.lanely.api.repository.TourStatusHistoryRepository;
import com.lanely.api.repository.VehicleRepository;
import com.lanely.api.repository.WaybillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class TourService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("reference", "name", "status", "plannedDate", "createdAt", "updatedAt");

    private final CompanyService companyService;
    private final TourRepository tourRepository;
    private final TourStatusHistoryRepository tourStatusHistoryRepository;
    private final WaybillRepository waybillRepository;
    private final VehicleRepository vehicleRepository;
    private final AccountRepository accountRepository;
    private final AssigneeResolver assigneeResolver;
    private final GeocodingService geocodingService;
    private final RoutingService routingService;
    private final OptimizationClient optimizationClient;

    public TourService(CompanyService companyService, TourRepository tourRepository,
                       TourStatusHistoryRepository tourStatusHistoryRepository,
                       WaybillRepository waybillRepository, VehicleRepository vehicleRepository,
                       AccountRepository accountRepository, AssigneeResolver assigneeResolver,
                       GeocodingService geocodingService,
                       RoutingService routingService, OptimizationClient optimizationClient) {
        this.companyService = companyService;
        this.tourRepository = tourRepository;
        this.tourStatusHistoryRepository = tourStatusHistoryRepository;
        this.waybillRepository = waybillRepository;
        this.vehicleRepository = vehicleRepository;
        this.accountRepository = accountRepository;
        this.assigneeResolver = assigneeResolver;
        this.geocodingService = geocodingService;
        this.routingService = routingService;
        this.optimizationClient = optimizationClient;
    }

    // ----- Web (User, MANAGE_TRANSPORTS) -----

    @Transactional
    public TourResponse createTour(UUID currentUserId, UUID companyId, CreateTourRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Company company = membership.getCompany();

        Tour tour = new Tour();
        tour.setCompany(company);
        tour.setReference(resolveNewReference(companyId, request.reference()));
        tour.setName(request.name().trim());
        tour.setStatus(TourStatus.PLANNED);
        tour.setPlannedDate(request.plannedDate());
        tour.setNotes(CompanyMapper.blankToNull(request.notes()));

        Address depot = request.depot() != null ? CompanyMapper.toAddress(request.depot())
                : copyAddress(company.getBillingAddress());
        tour.setDepot(depot);
        tour.setDepotLocation(resolveDepotLocation(request.depotLocation(), depot));

        if (request.vehicleId() != null) {
            tour.setVehicle(resolveVehicle(companyId, request.vehicleId()));
        }
        if (request.assignedAccountId() != null) {
            tour.setAssignedAccount(assigneeResolver.resolveAssignee(companyId, request.assignedAccountId()));
            tour.setStatus(TourStatus.ASSIGNED);
        }
        recordStatusHistory(tour, null, tour.getStatus(), membership.getUser(), null, null, null);
        tourRepository.save(tour);
        return toResponse(tour);
    }

    @Transactional(readOnly = true)
    public PageResponse<TourSummaryResponse> listTours(UUID currentUserId, UUID companyId, TourStatus status,
                                                       LocalDate plannedDate, UUID assignedAccountId, String q,
                                                       Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        validateSort(pageable.getSort());
        Page<TourSummaryResponse> page = tourRepository
                .search(companyId, status, plannedDate, assignedAccountId, searchPattern(q), pageable)
                .map(TourMapper::toSummary);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public TourResponse getTour(UUID currentUserId, UUID companyId, UUID tourId) {
        companyService.requireMember(companyId, currentUserId);
        return toResponse(loadTour(companyId, tourId));
    }

    @Transactional
    public TourResponse updateTour(UUID currentUserId, UUID companyId, UUID tourId, UpdateTourRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Tour tour = loadTour(companyId, tourId);
        if (request.name() != null) {
            tour.setName(request.name().trim());
        }
        if (request.plannedDate() != null) {
            tour.setPlannedDate(request.plannedDate());
        }
        if (request.depot() != null) {
            tour.setDepot(CompanyMapper.toAddress(request.depot()));
            tour.setDepotLocation(resolveDepotLocation(request.depotLocation(), tour.getDepot()));
        } else if (request.depotLocation() != null && request.depotLocation().isComplete()) {
            tour.setDepotLocation(new GeoPoint(request.depotLocation().latitude(), request.depotLocation().longitude()));
        }
        if (request.notes() != null) {
            tour.setNotes(CompanyMapper.blankToNull(request.notes()));
        }
        return toResponse(tour);
    }

    @Transactional
    public TourResponse assign(UUID currentUserId, UUID companyId, UUID tourId, AssignTourRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Tour tour = loadTour(companyId, tourId);
        tour.setVehicle(request.vehicleId() == null ? null : resolveVehicle(companyId, request.vehicleId()));
        tour.setAssignedAccount(request.assignedAccountId() == null
                ? null : assigneeResolver.resolveAssignee(companyId, request.assignedAccountId()));
        if (tour.getAssignedAccount() != null && tour.getStatus() == TourStatus.PLANNED) {
            applyStatus(tour, TourStatus.ASSIGNED, membership.getUser(), null, null, null);
        } else if (tour.getAssignedAccount() == null && tour.getStatus() == TourStatus.ASSIGNED) {
            applyStatus(tour, TourStatus.PLANNED, membership.getUser(), null, null, null);
        }
        return toResponse(tour);
    }

    @Transactional
    public TourResponse setWaybills(UUID currentUserId, UUID companyId, UUID tourId, SetTourWaybillsRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Tour tour = loadTour(companyId, tourId);

        List<Waybill> current = waybillRepository.findByTourIdOrderByPositionInTourAsc(tourId);
        for (Waybill existing : current) {
            if (!request.waybillIds().contains(existing.getId())) {
                existing.setTour(null);
                existing.setPositionInTour(null);
            }
        }
        int position = 0;
        for (UUID waybillId : request.waybillIds()) {
            Waybill waybill = waybillRepository.findByIdAndCompanyId(waybillId, companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.waybill.not-found"));
            waybill.setTour(tour);
            waybill.setPositionInTour(position++);
        }
        tour.setRoute(safeRouteForTour(tour, orderedWaybills(request.waybillIds(), companyId)));
        return toResponse(tour);
    }

    @Transactional
    public TourResponse changeStatus(UUID currentUserId, UUID companyId, UUID tourId, ChangeTourStatusRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Tour tour = loadTour(companyId, tourId);
        applyStatus(tour, request.status(), membership.getUser(), request.note(), request.latitude(), request.longitude());
        return toResponse(tour);
    }

    @Transactional(readOnly = true)
    public PageResponse<StatusHistoryResponse> getStatusHistory(UUID currentUserId, UUID companyId, UUID tourId,
                                                                Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        loadTour(companyId, tourId);
        return statusHistoryPage(tourId, pageable);
    }

    @Transactional
    public TourResponse deleteTour(UUID currentUserId, UUID companyId, UUID tourId) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Tour tour = loadTour(companyId, tourId);
        applyStatus(tour, TourStatus.CANCELLED, membership.getUser(), null, null, null);
        for (Waybill waybill : waybillRepository.findByTourIdOrderByPositionInTourAsc(tourId)) {
            waybill.setTour(null);
            waybill.setPositionInTour(null);
        }
        return toResponse(tour);
    }

    @Transactional
    public TourOptimizeResponse optimize(UUID currentUserId, UUID companyId, UUID tourId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Tour tour = loadTour(companyId, tourId);
        GeoPoint depot = requireDepotLocation(tour);

        List<Waybill> waybills = waybillRepository.findByTourIdOrderByPositionInTourAsc(tourId);
        List<OrsVisit> visits = new ArrayList<>();
        for (Waybill waybill : waybills) {
            GeoPoint location = waybill.getDeliveryLocation();
            if (location != null && location.isComplete()) {
                visits.add(new OrsVisit(waybill.getId().toString(), waybill.getReference(),
                        location.getLatitude(), location.getLongitude(), 0, 0));
            }
        }
        if (visits.isEmpty()) {
            throw new BadRequestException("error.tour.no-routable-stops");
        }

        OrsOptimizeRequest orsRequest = new OrsOptimizeRequest(
                new OrsCoordinate(depot.getLatitude(), depot.getLongitude()),
                1, null, null, Boolean.TRUE, "POLYLINE", visits);
        OrsOptimizeResponse response = optimizationClient.optimize(orsRequest);

        applyOptimizedOrder(waybills, response);
        applyTourRoute(tour, response);
        tour.setLastOptimizedAt(Instant.now());

        List<SkippedVisitDto> skipped = GeoMapper.toSkipped(response);
        return new TourOptimizeResponse(toResponse(tour), skipped);
    }

    @Transactional(readOnly = true)
    public RoutePreviewResponse previewRoute(UUID currentUserId, UUID companyId, UUID tourId, RoutePreviewRequest request) {
        companyService.requireMember(companyId, currentUserId);
        Tour tour = loadTour(companyId, tourId);
        GeoPoint depot = requireDepotLocation(tour);

        Map<UUID, Waybill> byId = new LinkedHashMap<>();
        for (Waybill waybill : waybillRepository.findByTourIdOrderByPositionInTourAsc(tourId)) {
            byId.put(waybill.getId(), waybill);
        }
        List<GeoPoint> points = new ArrayList<>();
        points.add(depot);
        for (UUID waybillId : request.waybillIds()) {
            Waybill waybill = byId.get(waybillId);
            if (waybill == null) {
                throw new BadRequestException("error.tour.waybill-not-in-tour");
            }
            points.add(waybill.getDeliveryLocation());
        }
        points.add(depot);

        RouteInfo route = routingService.routeOrdered(points);
        return new RoutePreviewResponse(request.waybillIds(), route.getDistanceMeters(),
                route.getDurationSeconds(), route.getGeometryPolyline());
    }

    @Transactional
    public TourResponse saveOrder(UUID currentUserId, UUID companyId, UUID tourId, RoutePreviewRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Tour tour = loadTour(companyId, tourId);

        Map<UUID, Waybill> byId = new LinkedHashMap<>();
        for (Waybill waybill : waybillRepository.findByTourIdOrderByPositionInTourAsc(tourId)) {
            byId.put(waybill.getId(), waybill);
        }
        int position = 0;
        for (UUID waybillId : request.waybillIds()) {
            Waybill waybill = byId.get(waybillId);
            if (waybill == null) {
                throw new BadRequestException("error.tour.waybill-not-in-tour");
            }
            waybill.setPositionInTour(position++);
        }
        tour.setRoute(safeRouteForTour(tour, orderedWaybills(request.waybillIds(), companyId)));
        return toResponse(tour);
    }

    // ----- Assignee surface (mobile Profile via /profile, web User via /companies/{id}/assignments) -----

    @Transactional(readOnly = true)
    public PageResponse<TourSummaryResponse> listForAssignee(UUID companyId, UUID accountId, TourStatus status,
                                                             Pageable pageable) {
        validateSort(pageable.getSort());
        Page<TourSummaryResponse> page = tourRepository
                .findAssignedToAccount(companyId, accountId, status, pageable)
                .map(TourMapper::toSummary);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public TourResponse getForAssignee(UUID companyId, UUID accountId, UUID tourId) {
        return toResponse(loadAssignedTour(companyId, accountId, tourId));
    }

    @Transactional
    public TourResponse changeStatusForAssignee(UUID companyId, UUID accountId, UUID tourId,
                                                ChangeTourStatusRequest request) {
        Tour tour = loadAssignedTour(companyId, accountId, tourId);
        Account actor = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("error.account.not-found"));
        applyStatus(tour, request.status(), actor, request.note(), request.latitude(), request.longitude());
        return toResponse(tour);
    }

    @Transactional(readOnly = true)
    public PageResponse<StatusHistoryResponse> getStatusHistoryForAssignee(UUID companyId, UUID accountId, UUID tourId,
                                                                           Pageable pageable) {
        loadAssignedTour(companyId, accountId, tourId);
        return statusHistoryPage(tourId, pageable);
    }

    // ----- Helpers -----

    private TourResponse toResponse(Tour tour) {
        return TourMapper.toResponse(tour, waybillRepository.findByTourIdOrderByPositionInTourAsc(tour.getId()));
    }

    private void applyOptimizedOrder(List<Waybill> waybills, OrsOptimizeResponse response) {
        Map<String, Waybill> byId = new LinkedHashMap<>();
        for (Waybill waybill : waybills) {
            byId.put(waybill.getId().toString(), waybill);
        }
        int position = 0;
        Set<String> placed = new java.util.HashSet<>();
        if (response.routes() != null) {
            for (OrsRoute route : response.routes()) {
                if (route.stops() == null) {
                    continue;
                }
                for (OrsStop stop : route.stops()) {
                    Waybill waybill = byId.get(stop.visitId());
                    if (waybill != null) {
                        waybill.setPositionInTour(position++);
                        placed.add(stop.visitId());
                    }
                }
            }
        }
        for (Waybill waybill : waybills) {
            if (!placed.contains(waybill.getId().toString())) {
                waybill.setPositionInTour(position++);
            }
        }
    }

    private void applyTourRoute(Tour tour, OrsOptimizeResponse response) {
        RouteInfo route = new RouteInfo();
        route.setDistanceMeters(response.totalDistanceMeters());
        route.setDurationSeconds(response.totalDrivingTimeSeconds());
        if (response.routes() != null && !response.routes().isEmpty()) {
            route.setGeometryPolyline(response.routes().get(0).geometryPolyline());
        }
        route.setComputedAt(Instant.now());
        tour.setRoute(route);
    }

    private List<Waybill> orderedWaybills(List<UUID> ids, UUID companyId) {
        List<Waybill> ordered = new ArrayList<>();
        for (UUID id : ids) {
            waybillRepository.findByIdAndCompanyId(id, companyId).ifPresent(ordered::add);
        }
        return ordered;
    }

    private RouteInfo safeRouteForTour(Tour tour, List<Waybill> orderedWaybills) {
        try {
            GeoPoint depot = tour.getDepotLocation();
            if (depot == null || !depot.isComplete()) {
                depot = geocodingService.geocodeBest(tour.getDepot());
            }
            if (depot == null || !depot.isComplete()) {
                return new RouteInfo();
            }
            List<GeoPoint> points = new ArrayList<>();
            points.add(depot);
            orderedWaybills.forEach(w -> points.add(w.getDeliveryLocation()));
            points.add(depot);
            return routingService.routeOrdered(points);
        } catch (ApiException ex) {
            return new RouteInfo();
        }
    }

    private GeoPoint requireDepotLocation(Tour tour) {
        GeoPoint depot = tour.getDepotLocation();
        if (depot != null && depot.isComplete()) {
            return depot;
        }
        GeoPoint geocoded = safeGeocode(tour.getDepot());
        if (geocoded.isComplete()) {
            tour.setDepotLocation(geocoded);
            return geocoded;
        }
        throw new BadRequestException("error.tour.depot-required");
    }

    private GeoPoint resolveDepotLocation(com.lanely.api.dto.geo.CoordinateDto location, Address depot) {
        if (location != null && location.isComplete()) {
            return new GeoPoint(location.latitude(), location.longitude());
        }
        return safeGeocode(depot);
    }

    private GeoPoint safeGeocode(Address address) {
        try {
            return geocodingService.geocodeBest(address);
        } catch (ApiException ex) {
            return new GeoPoint();
        }
    }

    private void applyStatus(Tour tour, TourStatus target, Account actor, String note, Double latitude, Double longitude) {
        TourStatus current = tour.getStatus();
        if (current == target) {
            return;
        }
        tour.setStatus(target);
        if (target == TourStatus.IN_PROGRESS && tour.getStartedAt() == null) {
            tour.setStartedAt(Instant.now());
        }
        if (target == TourStatus.COMPLETED) {
            tour.setCompletedAt(Instant.now());
        }
        recordStatusHistory(tour, current, target, actor, note, latitude, longitude);
    }

    private void recordStatusHistory(Tour tour, TourStatus fromStatus, TourStatus toStatus, Account actor,
                                     String note, Double latitude, Double longitude) {
        TourStatusHistory entry = new TourStatusHistory();
        entry.setTour(tour);
        entry.setFromStatus(fromStatus);
        entry.setToStatus(toStatus);
        entry.setChangedBy(actor);
        entry.setChangedByName(StatusHistorySupport.actorName(actor));
        entry.setActorType(StatusHistorySupport.actorType(actor));
        entry.setNote(CompanyMapper.blankToNull(note));
        entry.setLatitude(latitude);
        entry.setLongitude(longitude);
        entry.setChangedAt(Instant.now());
        tour.getStatusHistory().add(entry);
    }

    private PageResponse<StatusHistoryResponse> statusHistoryPage(UUID tourId, Pageable pageable) {
        Page<StatusHistoryResponse> page = tourStatusHistoryRepository
                .findByTourIdOrderByChangedAtDesc(tourId, pageable)
                .map(StatusHistoryMapper::toResponse);
        return PageResponse.of(page);
    }

    private Vehicle resolveVehicle(UUID companyId, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new TransportAssignmentException("error.tour.vehicle-other-company"));
        return vehicle;
    }

    private Tour loadTour(UUID companyId, UUID tourId) {
        return tourRepository.findByIdAndCompanyId(tourId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.tour.not-found"));
    }

    private Tour loadAssignedTour(UUID companyId, UUID accountId, UUID tourId) {
        return tourRepository.findByIdAndCompanyIdAndAssignedAccountId(tourId, companyId, accountId)
                .orElseThrow(() -> new ForbiddenAssignmentException("error.tour.not-assigned"));
    }

    private String resolveNewReference(UUID companyId, String requested) {
        String reference = CompanyMapper.blankToNull(requested);
        if (reference != null) {
            if (tourRepository.existsByCompanyIdAndReference(companyId, reference)) {
                throw new TourReferenceTakenException("error.tour.reference-taken");
            }
            return reference;
        }
        long next = tourRepository.countByCompanyId(companyId) + 1;
        String generated;
        do {
            generated = String.format("TUR-%04d", next++);
        } while (tourRepository.existsByCompanyIdAndReference(companyId, generated));
        return generated;
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new BadRequestException("error.sort.invalid", order.getProperty());
            }
        }
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

}

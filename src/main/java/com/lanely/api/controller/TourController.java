package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.common.StatusHistoryResponse;
import com.lanely.api.dto.error.ErrorResponse;
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
import com.lanely.api.entity.enums.TourStatus;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.TourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/tours")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tours", description = "Manage tours (rounds): group waybills, assign a vehicle and a driver, optimize and route them")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @PostMapping
    @Operation(summary = "Create a tour",
            description = "Creates a tour in PLANNED status (ASSIGNED if a driver profile is provided). Requires the MANAGE_TRANSPORTS "
                    + "permission. The reference is auto-generated (e.g. TUR-0001) when omitted. The depot defaults to the company billing "
                    + "address; its coordinates are geocoded when not provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tour created",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Reference conflict, or vehicle/profile from another company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TourResponse> create(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateTourRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        TourResponse response = tourService.createTour(principal.userId(), companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List tours",
            description = "Returns a paginated list of the company's tours. Optionally filter by status, planned date, assigned profile "
                    + "and a free-text query on reference or name. The caller must be a member of the company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of tours",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<TourSummaryResponse> list(
            @PathVariable UUID companyId,
            @Parameter(description = "Filter by status", example = "PLANNED")
            @RequestParam(required = false) TourStatus status,
            @Parameter(description = "Filter by planned date (ISO-8601)", example = "2026-06-24")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Filter by assigned driver account identifier (mobile profile or web user)")
            @RequestParam(required = false) UUID assignedAccountId,
            @Parameter(description = "Free-text search on reference or name", example = "Rennes")
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.listTours(principal.userId(), companyId, status, date, assignedAccountId, q, pageable);
    }

    @GetMapping("/{tourId}")
    @Operation(summary = "Get a tour", description = "Returns the tour with its ordered waybills and computed route.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tour returned",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or tour not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse get(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.getTour(principal.userId(), companyId, tourId);
    }

    @PatchMapping("/{tourId}")
    @Operation(summary = "Update a tour",
            description = "Updates a tour's editable fields. Only non-null fields are applied. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tour updated",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or tour not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse update(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @Valid @RequestBody UpdateTourRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.updateTour(principal.userId(), companyId, tourId, request);
    }

    @PostMapping("/{tourId}/assign")
    @Operation(summary = "Assign a vehicle and driver",
            description = "Assigns a vehicle and/or a driver profile to the tour. A null field clears that assignment. Assigning a driver "
                    + "to a PLANNED tour moves it to ASSIGNED. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tour assigned",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or tour not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Vehicle or profile belongs to another company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse assign(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @Valid @RequestBody AssignTourRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.assign(principal.userId(), companyId, tourId, request);
    }

    @PostMapping("/{tourId}/waybills")
    @Operation(summary = "Set the tour waybills",
            description = "Sets the ordered list of waybills that make up the tour. Waybills not in the list are detached; the list order "
                    + "becomes the stop order. Recomputes the tour route (best-effort). Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Waybills set",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, tour or a waybill not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse setWaybills(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @Valid @RequestBody SetTourWaybillsRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.setWaybills(principal.userId(), companyId, tourId, request);
    }

    @PostMapping("/{tourId}/optimize")
    @Operation(summary = "Optimize the tour",
            description = "Computes the optimal stop order (Timefold VRP/TSP) from the depot and applies it to the waybills, persisting the "
                    + "full-tour route. Stops that cannot be attached to the road network are returned in skippedVisits and keep their "
                    + "previous position. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tour optimized",
                    content = @Content(schema = @Schema(implementation = TourOptimizeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing depot location or no routable stops",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or tour not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Optimization call failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Routing subsystem unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourOptimizeResponse optimize(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.optimize(principal.userId(), companyId, tourId);
    }

    @PostMapping("/{tourId}/route/preview")
    @Operation(summary = "Preview a route order (no save)",
            description = "Computes the route (distance, duration, geometry) for a custom stop order WITHOUT persisting anything. Used for "
                    + "real-time display while the user reorders the tour on the frontend. The caller must be a member of the company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route previewed",
                    content = @Content(schema = @Schema(implementation = RoutePreviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "A waybill is not part of this tour, or depot location missing",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or tour not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Routing subsystem unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RoutePreviewResponse previewRoute(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @Valid @RequestBody RoutePreviewRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.previewRoute(principal.userId(), companyId, tourId, request);
    }

    @PostMapping("/{tourId}/route")
    @Operation(summary = "Save a route order",
            description = "Persists the given stop order for the tour and recomputes/persists the tour route. Requires the "
                    + "MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order saved",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "400", description = "A waybill is not part of this tour",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or tour not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse saveOrder(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @Valid @RequestBody RoutePreviewRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.saveOrder(principal.userId(), companyId, tourId, request);
    }

    @PostMapping("/{tourId}/status")
    @Operation(summary = "Change a tour status",
            description = "Sets the tour to any status (PLANNED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED). There is no lifecycle "
                    + "restriction: any status can be set from any other, including moving backward (e.g. COMPLETED back to IN_PROGRESS) or "
                    + "out of a terminal status, to allow corrections. Setting the same status is a no-op. Reaching IN_PROGRESS/COMPLETED "
                    + "sets the matching timestamps. Every effective change is recorded in the status history. Requires the "
                    + "MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or tour not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse changeStatus(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @Valid @RequestBody ChangeTourStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.changeStatus(principal.userId(), companyId, tourId, request);
    }

    @GetMapping("/{tourId}/status-history")
    @Operation(summary = "List a tour status history",
            description = "Returns the paginated status-change history of a tour, newest first. Each entry records the transition "
                    + "(fromStatus/toStatus), the account that performed it (a company user or a driver profile), the instant of the change "
                    + "(ISO-8601 UTC) and the optional note and GPS coordinates. The initial creation entry has a null fromStatus. The "
                    + "caller must be a member of the company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of status-history entries",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or tour not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<StatusHistoryResponse> statusHistory(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.getStatusHistory(principal.userId(), companyId, tourId, pageable);
    }

    @DeleteMapping("/{tourId}")
    @Operation(summary = "Cancel a tour",
            description = "Cancels the tour (moves it to CANCELLED) and detaches its waybills. The cancellation is recorded in the status "
                    + "history. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tour cancelled",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or tour not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse cancel(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tourService.deleteTour(principal.userId(), companyId, tourId);
    }
}

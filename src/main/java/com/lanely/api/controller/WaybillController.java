package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.common.StatusHistoryResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.pricing.QuoteResponse;
import com.lanely.api.dto.pricing.RecalculateWaybillPriceRequest;
import com.lanely.api.dto.waybill.AssignWaybillRequest;
import com.lanely.api.dto.waybill.BulkArchiveRequest;
import com.lanely.api.dto.waybill.BulkCancelRequest;
import com.lanely.api.dto.waybill.BulkResultResponse;
import com.lanely.api.dto.waybill.BulkStatusRequest;
import com.lanely.api.dto.waybill.ChangeParcelStatusRequest;
import com.lanely.api.dto.waybill.ChangeWaybillStatusRequest;
import com.lanely.api.dto.waybill.CreateWaybillRequest;
import com.lanely.api.dto.waybill.SignatureDto;
import com.lanely.api.dto.waybill.UpdateWaybillRequest;
import com.lanely.api.dto.waybill.WaybillDateField;
import com.lanely.api.dto.waybill.WaybillResponse;
import com.lanely.api.dto.waybill.WaybillSummaryResponse;
import com.lanely.api.entity.enums.WaybillStatus;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.PricingService;
import com.lanely.api.service.WaybillService;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/waybills")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Waybills", description = "Manage waybills (consignment notes, CMR/eCMR) of a transport company")
public class WaybillController {

    private final WaybillService waybillService;
    private final PricingService pricingService;

    public WaybillController(WaybillService waybillService, PricingService pricingService) {
        this.waybillService = waybillService;
        this.pricingService = pricingService;
    }

    @PostMapping("/{waybillId}/pricing/recalculate")
    @Operation(
            summary = "Recalculate and store the waybill's carriage charges",
            description = "Recomputes the waybill's price from a resolved rate card and writes the result into carriageChargesAmount and "
                    + "carriageChargesCurrency. Requires the MANAGE_TRANSPORTS permission. The rate card is resolved from an explicit tariffId, "
                    + "else the waybill client's ACTIVE grid valid today, else the company ACTIVE default grid. Quantities come from the waybill "
                    + "(route distance, total weight, volume, packages). The stored amount and the returned breakdown are tax-excluded (HT). "
                    + "Cannot be applied to a CANCELLED waybill. Returns the full quote breakdown "
                    + "that was stored; non-fatal issues (e.g. route distance not computed, no fuel index) are reported in its warnings."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Price recomputed and stored; quote breakdown returned",
                    content = @Content(schema = @Schema(implementation = QuoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "No applicable rate card, or the waybill is CANCELLED",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, waybill or forced rate card not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public QuoteResponse recalculatePrice(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the waybill", example = "5d2c1b3a-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID waybillId,
            @Valid @RequestBody(required = false) RecalculateWaybillPriceRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        UUID tariffId = request == null ? null : request.tariffId();
        return pricingService.recalculateWaybill(principal.userId(), companyId, waybillId, tariffId);
    }

    @PostMapping
    @Operation(summary = "Create a waybill",
            description = "Creates a waybill in DRAFT status. Requires the MANAGE_TRANSPORTS permission. A waybill is always tied to an "
                    + "ordering customer (donneur d'ordre) via the mandatory clientId; that customer also bears the carriage charges. The "
                    + "reference is unique within the company and auto-generated (e.g. WBL-0001) when omitted. Each party "
                    + "(shipper/consignee) is a frozen snapshot that can be filled freely or pre-filled from a client via "
                    + "clientId/clientAddressId. The places of taking over and delivery can each be a free address, linked to a client, or "
                    + "linked to a client with an overriding custom address. When a place has no coordinates and none can be derived from a "
                    + "linked client address, the server geocodes the resolved address and returns 400 when it cannot be located.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Waybill created",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, or a place address could not be geocoded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, ordering customer, linked client/address, tour or profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Reference already used in this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Geocoding subsystem unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WaybillResponse> create(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateWaybillRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        WaybillResponse response = waybillService.createWaybill(principal.userId(), companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List waybills",
            description = "Returns a paginated list of the company's waybills. The caller must be a member of the company. "
                    + "Optionally filter by status, tour, assigned profile, ordering customer (donneur d'ordre) and a "
                    + "free-text query on reference or party names. "
                    + "The status filter is repeatable (?status=ISSUED&status=IN_TRANSIT): a waybill matches when its status is in "
                    + "the supplied set (OR); a single status keeps working, and no status means no status filter. "
                    + "By default only non-archived waybills are returned; use the archived parameter to change that. "
                    + "The list can also be filtered by a planned-date range: dateFrom (inclusive) and/or dateTo (exclusive) "
                    + "apply to the planned pickup instant, the planned delivery instant, or the dock-entry instant "
                    + "(date de passage à quai), selected by dateField (default PICKUP). "
                    + "The date filter is only applied when dateFrom or dateTo is provided; waybills whose chosen date is "
                    + "null are then excluded. Each summary carries the pickup and delivery cities and coordinates so the client "
                    + "can plot the list on a map without fetching each waybill, plus the dock-entry/exit instants. "
                    + "Allowed sort fields: reference, status, createdAt, updatedAt, pickupPlannedAt, deliveryPlannedAt, dockEnteredAt.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of waybills",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid sort field, invalid query parameter value (e.g. status, "
                    + "archived, dateField, dateFrom, dateTo), or dateFrom is after dateTo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<WaybillSummaryResponse> list(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Filter by status; repeatable. A waybill matches when its status is in the supplied set (OR). "
                    + "Omit for no status filter.", example = "ISSUED")
            @RequestParam(required = false) List<WaybillStatus> status,
            @Parameter(description = "Archived filter. 'false' (default) returns only non-archived waybills, 'true' only archived ones, "
                    + "'all' returns both.", example = "false")
            @RequestParam(required = false) String archived,
            @Parameter(description = "Free-text search on reference or party names", example = "WBL-0001")
            @RequestParam(required = false) String q,
            @Parameter(description = "Filter by tour identifier")
            @RequestParam(required = false) UUID tourId,
            @Parameter(description = "Filter by directly-assigned driver account identifier (mobile profile or web user)")
            @RequestParam(required = false) UUID assignedAccountId,
            @Parameter(description = "Filter by ordering customer (donneur d'ordre) identifier: returns only the waybills whose "
                    + "ordering client is this client.", example = "aaaa1111-2222-3333-4444-555566667777")
            @RequestParam(required = false) UUID clientId,
            @Parameter(description = "Which date the dateFrom/dateTo range filters on. PICKUP uses the planned pickup "
                    + "instant, DELIVERY the planned delivery instant, DOCK the dock-entry instant (date de passage à quai). "
                    + "Defaults to PICKUP when a date bound is provided without it.",
                    example = "PICKUP")
            @RequestParam(required = false) WaybillDateField dateField,
            @Parameter(description = "Inclusive lower bound (>=) of the chosen planned date, ISO-8601 UTC. Applied only when "
                    + "provided; combine with dateTo for a closed range.", example = "2026-06-25T00:00:00Z")
            @RequestParam(required = false) Instant dateFrom,
            @Parameter(description = "Exclusive upper bound (<) of the chosen planned date, ISO-8601 UTC. Applied only when "
                    + "provided; for a single day pass the next day's start.", example = "2026-06-26T00:00:00Z")
            @RequestParam(required = false) Instant dateTo,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.listWaybills(principal.userId(), companyId, status, archived, q, tourId, assignedAccountId,
                clientId, dateField, dateFrom, dateTo, pageable);
    }

    @GetMapping("/{waybillId}")
    @Operation(summary = "Get a waybill", description = "Returns the full detail of a waybill. The caller must be a member of the company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Waybill returned",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or waybill not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse get(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.getWaybill(principal.userId(), companyId, waybillId);
    }

    @PatchMapping("/{waybillId}")
    @Operation(summary = "Update a waybill",
            description = "Updates an editable waybill (DRAFT or ISSUED). Only non-null fields are applied; a provided parties or goods "
                    + "block replaces the existing one. The ordering customer (clientId) can be replaced but not cleared. When a place is "
                    + "provided without coordinates, the server geocodes its resolved address and returns 400 when it cannot be located. "
                    + "Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Waybill updated",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, or a place address could not be geocoded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, waybill or ordering customer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Waybill not editable, or reference conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Geocoding subsystem unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse update(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @Valid @RequestBody UpdateWaybillRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.updateWaybill(principal.userId(), companyId, waybillId, request);
    }

    @GetMapping("/{waybillId}/status-history")
    @Operation(summary = "List a waybill status history",
            description = "Returns the paginated status-change history of a waybill, newest first. Each entry records the transition "
                    + "(fromStatus/toStatus), the account that performed it (a company user or a driver profile), the instant of the change "
                    + "(ISO-8601 UTC) and the optional note and GPS coordinates captured at that moment. The initial creation entry has a "
                    + "null fromStatus. The caller must be a member of the company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of status-history entries",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or waybill not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<StatusHistoryResponse> statusHistory(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.getStatusHistory(principal.userId(), companyId, waybillId, pageable);
    }

    @PostMapping("/{waybillId}/status")
    @Operation(summary = "Change a waybill status",
            description = "Sets the waybill to any status (DRAFT, ISSUED, COLLECTED, IN_TRANSIT, DELIVERED, FAILED, CANCELLED). There is no "
                    + "lifecycle restriction: any status can be set from any other, including moving backward (e.g. DELIVERED back to "
                    + "COLLECTED) or out of a terminal status, to allow corrections. Setting the same status is a no-op. Moving to FAILED "
                    + "requires failureReason. Reaching COLLECTED/DELIVERED sets the matching actual timestamps. Every effective change is "
                    + "recorded in the status history. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing failure reason",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or waybill not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse changeStatus(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @Valid @RequestBody ChangeWaybillStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.changeStatus(principal.userId(), companyId, waybillId, request);
    }

    @PostMapping("/{waybillId}/parcels/{parcelId}/status")
    @Operation(summary = "Change a parcel status",
            description = "Sets a single parcel (goods line) of the waybill to any status (PENDING, LOADED, IN_TRANSIT, DELIVERED, FAILED, "
                    + "CANCELLED). There is no lifecycle restriction: any status can be set from any other, including backward, to allow "
                    + "corrections. Setting the same status is a no-op. Every effective change is recorded in the parcel status history. "
                    + "Returns the full waybill. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parcel status changed",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, waybill or parcel not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse changeParcelStatus(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @Parameter(description = "Identifier of the parcel (goods line) within the waybill")
            @PathVariable UUID parcelId,
            @Valid @RequestBody ChangeParcelStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.changeParcelStatus(principal.userId(), companyId, waybillId, parcelId, request);
    }

    @GetMapping("/{waybillId}/parcels/{parcelId}/status-history")
    @Operation(summary = "List a parcel status history",
            description = "Returns the paginated status-change history of a single parcel (goods line), newest first. Each entry records the "
                    + "transition (fromStatus/toStatus), the account that performed it, the instant of the change (ISO-8601 UTC) and the "
                    + "optional note and GPS coordinates. The caller must be a member of the company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of status-history entries",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, waybill or parcel not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<StatusHistoryResponse> parcelStatusHistory(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @PathVariable UUID parcelId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.getParcelStatusHistory(principal.userId(), companyId, waybillId, parcelId, pageable);
    }

    @PostMapping("/{waybillId}/signatures")
    @Operation(summary = "Add an eCMR signature",
            description = "Adds an electronic signature (eCMR) to the waybill. The signing instant is set by the server. "
                    + "Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Signature added",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or waybill not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WaybillResponse> addSignature(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @Valid @RequestBody SignatureDto request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        WaybillResponse response = waybillService.addSignature(principal.userId(), companyId, waybillId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{waybillId}/assign")
    @Operation(summary = "Assign a waybill",
            description = "Attaches the waybill to a tour and/or assigns it directly to a driver profile. A null field clears the "
                    + "corresponding assignment. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Waybill assigned",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or waybill not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Tour or profile belongs to another company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse assign(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @Valid @RequestBody AssignWaybillRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.assign(principal.userId(), companyId, waybillId, request);
    }

    @DeleteMapping("/{waybillId}")
    @Operation(summary = "Cancel a waybill",
            description = "Soft-cancels the waybill (moves it to CANCELLED). Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Waybill cancelled",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or waybill not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Waybill cannot be cancelled from its current status",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse cancel(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.cancel(principal.userId(), companyId, waybillId);
    }

    @PostMapping("/{waybillId}/archive")
    @Operation(summary = "Archive a waybill",
            description = "Archives the waybill: it is hidden from default lists but stays readable, and its business status is left "
                    + "unchanged (archiving is distinct from cancellation). Idempotent: archiving an already-archived waybill succeeds as "
                    + "a no-op. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Waybill archived",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or waybill not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse archive(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.archive(principal.userId(), companyId, waybillId);
    }

    @PostMapping("/{waybillId}/unarchive")
    @Operation(summary = "Unarchive a waybill",
            description = "Restores an archived waybill into the default lists. Its business status is left unchanged. Idempotent: "
                    + "unarchiving a non-archived waybill succeeds as a no-op. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Waybill unarchived",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or waybill not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse unarchive(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.unarchive(principal.userId(), companyId, waybillId);
    }

    @PostMapping("/bulk/status")
    @Operation(summary = "Bulk change waybill status",
            description = "Changes the status of up to 200 waybills in one call. The same per-item transition rules as the single-waybill "
                    + "status endpoint apply, and each effective change is recorded in the status history. A failure on one item does not "
                    + "abort the batch: the response always uses HTTP 200 and reports per-item outcomes (OK / ERROR with a code). Per-item "
                    + "error codes: WAYBILL_NOT_FOUND (id absent or outside the company), FAILURE_REASON_REQUIRED (status FAILED without "
                    + "failureReason), INVALID_TRANSITION. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch processed (inspect succeeded/failed and per-item results)",
                    content = @Content(schema = @Schema(implementation = BulkResultResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request: empty batch (BULK_EMPTY), more than 200 ids "
                    + "(BULK_TOO_MANY_ITEMS), or malformed body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BulkResultResponse bulkChangeStatus(
            @PathVariable UUID companyId,
            @Valid @RequestBody BulkStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.bulkChangeStatus(principal.userId(), companyId, request);
    }

    @PostMapping("/bulk/archive")
    @Operation(summary = "Bulk archive or unarchive waybills",
            description = "Archives (archived=true) or unarchives (archived=false) up to 200 waybills in one call. Idempotent per item. "
                    + "A failure on one item does not abort the batch: the response always uses HTTP 200 and reports per-item outcomes. "
                    + "Per-item error code: WAYBILL_NOT_FOUND. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch processed (inspect succeeded/failed and per-item results)",
                    content = @Content(schema = @Schema(implementation = BulkResultResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request: empty batch (BULK_EMPTY), more than 200 ids "
                    + "(BULK_TOO_MANY_ITEMS), or malformed body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BulkResultResponse bulkArchive(
            @PathVariable UUID companyId,
            @Valid @RequestBody BulkArchiveRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.bulkArchive(principal.userId(), companyId, request);
    }

    @PostMapping("/bulk/cancel")
    @Operation(summary = "Bulk cancel waybills",
            description = "Cancels (moves to CANCELLED) up to 200 waybills in one call. Each effective change is recorded in the status "
                    + "history. A failure on one item does not abort the batch: the response always uses HTTP 200 and reports per-item "
                    + "outcomes. Per-item error codes: WAYBILL_NOT_FOUND, INVALID_TRANSITION. Requires the MANAGE_TRANSPORTS permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch processed (inspect succeeded/failed and per-item results)",
                    content = @Content(schema = @Schema(implementation = BulkResultResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request: empty batch (BULK_EMPTY), more than 200 ids "
                    + "(BULK_TOO_MANY_ITEMS), or malformed body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BulkResultResponse bulkCancel(
            @PathVariable UUID companyId,
            @Valid @RequestBody BulkCancelRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.bulkCancel(principal.userId(), companyId, request);
    }
}

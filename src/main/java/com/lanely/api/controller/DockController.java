package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.waybill.DockSummaryResponse;
import com.lanely.api.dto.waybill.WaybillSummaryResponse;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.WaybillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/dock")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dock", description = "Access goods currently held at the dock (depot). A waybill or goods line at the dock is one in "
        + "the AT_DOCK status: collected goods waiting on the dock before being re-dispatched (cross-docking) or kept as stock.")
public class DockController {

    private final WaybillService waybillService;

    public DockController(WaybillService waybillService) {
        this.waybillService = waybillService;
    }

    @GetMapping
    @Operation(summary = "List goods at the dock",
            description = "Returns a paginated list of the company's waybills currently at the dock. The caller must be a member of "
                    + "the company. A waybill is considered at the dock when its status is AT_DOCK or when it holds at least one "
                    + "goods line in the AT_DOCK status (partial stock left behind while the waybill itself moved on). Archived "
                    + "waybills are excluded. "
                    + "Optionally filter by ordering customer (clientId), a free-text query on reference or party names (q), and a "
                    + "dock-entry date range: dockFrom (inclusive, >=) and/or dockTo (exclusive, <) apply to the waybill dock-entry "
                    + "instant (date de passage à quai). Each summary carries dockEnteredAt/dockExitedAt so the client can show how "
                    + "long the goods have been on the dock (dwell = now - dockEnteredAt). "
                    + "Allowed sort fields: reference, status, createdAt, updatedAt, pickupPlannedAt, deliveryPlannedAt, dockEnteredAt "
                    + "(sort by dockEnteredAt ascending to surface the oldest, dormant stock first).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of waybills at the dock",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid sort field, invalid query parameter value, or dockFrom is after dockTo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<WaybillSummaryResponse> list(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Free-text search on reference or party names", example = "WBL-0001")
            @RequestParam(required = false) String q,
            @Parameter(description = "Filter by ordering customer (donneur d'ordre) identifier",
                    example = "aaaa1111-2222-3333-4444-555566667777")
            @RequestParam(required = false) UUID clientId,
            @Parameter(description = "Inclusive lower bound (>=) of the dock-entry instant, ISO-8601 UTC. Applied only when provided.",
                    example = "2026-06-10T00:00:00Z")
            @RequestParam(required = false) Instant dockFrom,
            @Parameter(description = "Exclusive upper bound (<) of the dock-entry instant, ISO-8601 UTC. Applied only when provided; "
                    + "for a single day pass the next day's start.", example = "2026-06-11T00:00:00Z")
            @RequestParam(required = false) Instant dockTo,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.listDock(principal.userId(), companyId, q, clientId, dockFrom, dockTo, pageable);
    }

    @GetMapping("/summary")
    @Operation(summary = "Summarize the goods at the dock",
            description = "Returns aggregated totals for the goods currently at the dock of the company: number of waybills at the "
                    + "dock and the summed packages, gross weight (kg) and volume (m³) of the goods lines on the dock. Goods lines "
                    + "count toward the totals when the line is AT_DOCK or its waybill is AT_DOCK; archived waybills are excluded. "
                    + "Useful to display the current dock load. The caller must be a member of the company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dock totals",
                    content = @Content(schema = @Schema(implementation = DockSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public DockSummaryResponse summary(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return waybillService.dockSummary(principal.userId(), companyId);
    }
}

package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.pricing.CreateTariffComponentRequest;
import com.lanely.api.dto.pricing.CreateTariffRequest;
import com.lanely.api.dto.pricing.FuelSurchargePolicyResponse;
import com.lanely.api.dto.pricing.TariffResponse;
import com.lanely.api.dto.pricing.TariffSummaryResponse;
import com.lanely.api.dto.pricing.UpdateTariffComponentRequest;
import com.lanely.api.dto.pricing.UpdateTariffRequest;
import com.lanely.api.dto.pricing.UpsertFuelSurchargePolicyRequest;
import com.lanely.api.entity.enums.TariffStatus;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.TariffService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/tariffs")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tariffs", description = "Manage the rate cards (tariffs) of a transport company: billable components and fuel surcharge policies. All monetary amounts are tax-excluded (HT).")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @PostMapping
    @Operation(
            summary = "Create a rate card",
            description = "Creates a rate card (tariff) in DRAFT status. Requires the MANAGE_PRICING permission (the OWNER always has it). "
                    + "A rate card holds billable components (per km, per kg, per stop, flat, etc.) and an optional fuel surcharge policy, "
                    + "added through the sub-resources below. Set isDefault to make it the company fallback grid (only one ACTIVE default is "
                    + "allowed per company); a client-specific grid (clientId set) cannot be the default. Components and the fuel policy are "
                    + "added afterwards. The rate card is only used for pricing once activated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rate card created",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, invalid validity window, or a client-specific grid was marked default",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or referenced client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TariffResponse> createTariff(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateTariffRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        TariffResponse response = tariffService.createTariff(principal.userId(), companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List rate cards",
            description = "Returns a paginated list of the company's rate cards. The caller must be a member of the company. "
                    + "Optionally filter by status and by the client a grid is bound to. Sortable fields: name, status, createdAt, updatedAt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of rate cards returned",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<TariffSummaryResponse> listTariffs(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Filter by lifecycle status. Omit to return any status.", example = "ACTIVE")
            @RequestParam(required = false) TariffStatus status,
            @Parameter(description = "Filter by the client a grid is specific to. Omit to return any.", example = "7a1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @RequestParam(required = false) UUID clientId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tariffService.listTariffs(principal.userId(), companyId, status, clientId, pageable);
    }

    @GetMapping("/{tariffId}")
    @Operation(
            summary = "Get a rate card",
            description = "Returns the full detail of a rate card, including its ordered components and its fuel surcharge policy. "
                    + "The caller must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rate card returned",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or rate card not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TariffResponse getTariff(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tariffService.getTariff(principal.userId(), companyId, tariffId);
    }

    @PatchMapping("/{tariffId}")
    @Operation(
            summary = "Update a rate card",
            description = "Updates a rate card's editable fields. Only non-null fields are applied. Requires MANAGE_PRICING. "
                    + "To detach a client (make the grid company-wide) send clientId as the all-zero UUID "
                    + "00000000-0000-0000-0000-000000000000. Marking an ACTIVE grid as default fails if another ACTIVE default already exists."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rate card updated",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, invalid validity window, default conflict, or client-specific default",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, rate card or referenced client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TariffResponse updateTariff(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @Valid @RequestBody UpdateTariffRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tariffService.updateTariff(principal.userId(), companyId, tariffId, request);
    }

    @PostMapping("/{tariffId}/activate")
    @Operation(
            summary = "Activate a rate card",
            description = "Moves a rate card to ACTIVE so it can be used for pricing, from either DRAFT or a previously deactivated grid. "
                    + "Requires MANAGE_PRICING. Activating a default grid fails if another ACTIVE default already exists for the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rate card activated",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "400", description = "Another ACTIVE default rate card already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or rate card not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TariffResponse activateTariff(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tariffService.setStatus(principal.userId(), companyId, tariffId, TariffStatus.ACTIVE);
    }

    @PostMapping("/{tariffId}/deactivate")
    @Operation(
            summary = "Deactivate a rate card",
            description = "Moves a rate card back to DRAFT so it is no longer resolved for pricing. The grid and its components are kept "
                    + "and can be activated again later. Requires MANAGE_PRICING."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rate card deactivated (back to DRAFT)",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or rate card not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TariffResponse deactivateTariff(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tariffService.setStatus(principal.userId(), companyId, tariffId, TariffStatus.DRAFT);
    }

    @DeleteMapping("/{tariffId}")
    @Operation(
            summary = "Delete a rate card",
            description = "Permanently deletes a rate card with its components and fuel surcharge policy. Requires MANAGE_PRICING. "
                    + "Prefer deactivating when the grid should be kept for later reuse."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rate card deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or rate card not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteTariff(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        tariffService.deleteTariff(principal.userId(), companyId, tariffId);
        return ResponseEntity.noContent().build();
    }

    // ----- Components -----

    @PostMapping("/{tariffId}/components")
    @Operation(
            summary = "Add a billable component",
            description = "Appends a billable line to the rate card. Requires MANAGE_PRICING. The basis selects which quantity drives the "
                    + "line (PER_KM uses the route distance, PER_KG the total gross weight, PER_STOP the number of delivery points, FLAT/PER_WAYBILL "
                    + "a fixed 1, etc.). Optional includedQuantity (free allowance), min/max quantity and min/max amount refine the line. "
                    + "Returns the full updated rate card."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Component added; updated rate card returned",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or rate card not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TariffResponse> addComponent(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @Valid @RequestBody CreateTariffComponentRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        TariffResponse response = tariffService.addComponent(principal.userId(), companyId, tariffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{tariffId}/components/{componentId}")
    @Operation(
            summary = "Update a billable component",
            description = "Updates a rate card line. Only non-null fields are applied. Requires MANAGE_PRICING. Returns the full updated rate card."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Component updated; updated rate card returned",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, rate card or component not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TariffResponse updateComponent(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @Parameter(description = "Identifier of the component", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID componentId,
            @Valid @RequestBody UpdateTariffComponentRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tariffService.updateComponent(principal.userId(), companyId, tariffId, componentId, request);
    }

    @DeleteMapping("/{tariffId}/components/{componentId}")
    @Operation(
            summary = "Delete a billable component",
            description = "Removes a line from the rate card. Requires MANAGE_PRICING. Returns the full updated rate card."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Component removed; updated rate card returned",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, rate card or component not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TariffResponse deleteComponent(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @Parameter(description = "Identifier of the component", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID componentId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tariffService.deleteComponent(principal.userId(), companyId, tariffId, componentId);
    }

    // ----- Fuel surcharge policy -----

    @PutMapping("/{tariffId}/fuel-surcharge")
    @Operation(
            summary = "Set the fuel surcharge policy",
            description = "Creates or replaces the fuel indexation policy of a rate card. Requires MANAGE_PRICING. "
                    + "THRESHOLD_COMPONENTS requires thresholdPrice and applies the surchargeComponents lines when the current fuel price exceeds it. "
                    + "INDEXED_PERCENT requires referencePrice and dieselShareRatio and applies dieselShareRatio x (current - reference)/reference "
                    + "as a percentage of the subtotal. The current price comes from the latest stored fuel index for the selected fuelType."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy created or replaced",
                    content = @Content(schema = @Schema(implementation = FuelSurchargePolicyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or required mode fields are missing",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or rate card not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public FuelSurchargePolicyResponse upsertFuelPolicy(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @Valid @RequestBody UpsertFuelSurchargePolicyRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return tariffService.upsertFuelPolicy(principal.userId(), companyId, tariffId, request);
    }

    @DeleteMapping("/{tariffId}/fuel-surcharge")
    @Operation(
            summary = "Remove the fuel surcharge policy",
            description = "Deletes the fuel indexation policy of a rate card so quotes no longer add a fuel surcharge. Requires MANAGE_PRICING."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Policy removed (no-op if none existed)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or rate card not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteFuelPolicy(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID tariffId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        tariffService.deleteFuelPolicy(principal.userId(), companyId, tariffId);
        return ResponseEntity.noContent().build();
    }
}

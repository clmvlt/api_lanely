package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.common.StatusHistoryResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.waybill.ChangeParcelStatusRequest;
import com.lanely.api.dto.waybill.ChangeWaybillStatusRequest;
import com.lanely.api.dto.waybill.SignatureDto;
import com.lanely.api.dto.waybill.WaybillDateField;
import com.lanely.api.dto.waybill.WaybillResponse;
import com.lanely.api.dto.waybill.WaybillSummaryResponse;
import com.lanely.api.entity.enums.WaybillStatus;
import com.lanely.api.security.AuthenticatedProfile;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/profile/waybills")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Profile · Waybills", description = "Mobile app endpoints for a driver profile to handle the waybills assigned to it")
public class ProfileWaybillController {

    private final WaybillService waybillService;

    public ProfileWaybillController(WaybillService waybillService) {
        this.waybillService = waybillService;
    }

    @GetMapping
    @Operation(summary = "List my waybills",
            description = "Returns the waybills to handle for the current driver: those assigned directly to it, plus those of the tours "
                    + "assigned to it. Works for both a delivery profile and a web user acting as a driver. Optionally filter by status and "
                    + "by a date window (dateField + dateFrom/dateTo), e.g. to get only the waybills planned for a given day.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of waybills",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter value (e.g. status, dateField, dateFrom, dateTo) or "
                    + "dateFrom is after dateTo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a driver account",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<WaybillSummaryResponse> list(
            @Parameter(description = "Filter by status", example = "IN_TRANSIT")
            @RequestParam(required = false) WaybillStatus status,
            @Parameter(description = "Which date the dateFrom/dateTo range filters on. PICKUP uses the planned pickup instant, DELIVERY the "
                    + "planned delivery instant, DOCK the dock-entry instant. Defaults to PICKUP when a date bound is provided without it.",
                    example = "DELIVERY")
            @RequestParam(required = false) WaybillDateField dateField,
            @Parameter(description = "Inclusive lower bound (>=) of the chosen planned date, ISO-8601 UTC. To get a single day, pass that "
                    + "day's start (in the driver's time zone, converted to UTC) here and the next day's start in dateTo.",
                    example = "2026-06-25T00:00:00Z")
            @RequestParam(required = false) Instant dateFrom,
            @Parameter(description = "Exclusive upper bound (<) of the chosen planned date, ISO-8601 UTC.", example = "2026-06-26T00:00:00Z")
            @RequestParam(required = false) Instant dateTo,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return waybillService.listForAssignee(principal.companyId(), principal.profileId(), status, dateField, dateFrom,
                dateTo, pageable);
    }

    @GetMapping("/{waybillId}")
    @Operation(summary = "Get one of my waybills",
            description = "Returns a waybill assigned to the current profile. Returns 403 if the waybill is not assigned to it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Waybill returned",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse get(
            @PathVariable UUID waybillId,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return waybillService.getForAssignee(principal.companyId(), principal.profileId(), waybillId);
    }

    @GetMapping("/{waybillId}/status-history")
    @Operation(summary = "List the status history of one of my waybills",
            description = "Returns the paginated status-change history (newest first) of a waybill assigned to the current profile. Each "
                    + "entry records the transition (fromStatus/toStatus), the account that performed it, the instant of the change "
                    + "(ISO-8601 UTC) and the optional note and GPS coordinates captured at that moment. Returns 403 if the waybill is not "
                    + "assigned to this profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of status-history entries",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<StatusHistoryResponse> statusHistory(
            @PathVariable UUID waybillId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return waybillService.getStatusHistoryForAssignee(principal.companyId(), principal.profileId(), waybillId, pageable);
    }

    @PostMapping("/{waybillId}/status")
    @Operation(summary = "Change a waybill status",
            description = "Sets the waybill to any status (DRAFT, ISSUED, COLLECTED, IN_TRANSIT, DELIVERED, FAILED, CANCELLED). There is no "
                    + "lifecycle restriction: any status can be set from any other, including moving backward or out of a terminal status, "
                    + "to allow corrections from the field. Setting the same status is a no-op. Moving to FAILED requires failureReason. "
                    + "Reaching COLLECTED/DELIVERED sets the matching actual timestamps. Every effective change is recorded in the status "
                    + "history.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing failure reason",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse changeStatus(
            @PathVariable UUID waybillId,
            @Valid @RequestBody ChangeWaybillStatusRequest request,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return waybillService.changeStatusForAssignee(principal.companyId(), principal.profileId(), waybillId, request);
    }

    @PostMapping("/{waybillId}/parcels/{parcelId}/status")
    @Operation(summary = "Change a parcel status",
            description = "Sets a single parcel (goods line) of a waybill assigned to the current profile to any status (PENDING, LOADED, "
                    + "IN_TRANSIT, DELIVERED, FAILED, CANCELLED). No lifecycle restriction: any status from any other, including backward. "
                    + "Setting the same status is a no-op. Every effective change is recorded in the parcel status history. Returns the full "
                    + "waybill.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parcel status changed",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parcel not found in this waybill",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse changeParcelStatus(
            @PathVariable UUID waybillId,
            @Parameter(description = "Identifier of the parcel (goods line) within the waybill")
            @PathVariable UUID parcelId,
            @Valid @RequestBody ChangeParcelStatusRequest request,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return waybillService.changeParcelStatusForAssignee(principal.companyId(), principal.profileId(), waybillId,
                parcelId, request);
    }

    @GetMapping("/{waybillId}/parcels/{parcelId}/status-history")
    @Operation(summary = "List the status history of one of my parcels",
            description = "Returns the paginated status-change history (newest first) of a single parcel (goods line) of a waybill assigned "
                    + "to the current profile. Returns 403 if the waybill is not assigned to this profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of status-history entries",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parcel not found in this waybill",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<StatusHistoryResponse> parcelStatusHistory(
            @PathVariable UUID waybillId,
            @PathVariable UUID parcelId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return waybillService.getParcelStatusHistoryForAssignee(principal.companyId(), principal.profileId(), waybillId,
                parcelId, pageable);
    }

    @PostMapping("/{waybillId}/signatures")
    @Operation(summary = "Capture an eCMR signature",
            description = "Captures the recipient's electronic signature (eCMR) on a waybill assigned to the current profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Signature captured",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WaybillResponse> addSignature(
            @PathVariable UUID waybillId,
            @Valid @RequestBody SignatureDto request,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        WaybillResponse response = waybillService.addSignatureForAssignee(principal.companyId(), principal.profileId(),
                waybillId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{waybillId}/proof", consumes = "multipart/form-data")
    @Operation(summary = "Upload a proof of delivery",
            description = "Uploads a proof-of-delivery photo for a waybill assigned to the current profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proof uploaded",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing or unsupported image",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse uploadProof(
            @PathVariable UUID waybillId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return waybillService.addProofForAssignee(principal.companyId(), principal.profileId(), waybillId, file);
    }
}

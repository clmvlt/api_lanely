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
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.CompanyService;
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
@RequestMapping("/companies/{companyId}/assignments/waybills")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Driver (Web user) · Waybills",
        description = "Mobile endpoints for a web user, acting as a driver in a given company, to handle the waybills assigned to it. "
                + "Mirror of the Profile · Waybills endpoints, scoped by companyId because a web user may belong to several companies.")
public class CompanyDriverWaybillController {

    private final WaybillService waybillService;
    private final CompanyService companyService;

    public CompanyDriverWaybillController(WaybillService waybillService, CompanyService companyService) {
        this.waybillService = waybillService;
        this.companyService = companyService;
    }

    @GetMapping
    @Operation(summary = "List the waybills assigned to me in this company",
            description = "Returns the waybills to handle for the current web user within the given company: those assigned directly to "
                    + "it, plus those of the tours assigned to it. The caller must be a member of the company. Optionally filter by status "
                    + "and by a date window (dateField + dateFrom/dateTo), e.g. to get only the waybills planned for a given day.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of waybills",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter value (e.g. status, dateField, dateFrom, dateTo) or "
                    + "dateFrom is after dateTo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of the company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<WaybillSummaryResponse> list(
            @PathVariable UUID companyId,
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
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return waybillService.listForAssignee(companyId, principal.userId(), status, dateField, dateFrom, dateTo, pageable);
    }

    @GetMapping("/{waybillId}")
    @Operation(summary = "Get one of the waybills assigned to me in this company",
            description = "Returns a waybill assigned to the current web user. Returns 403 if the waybill is not assigned to it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Waybill returned",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse get(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return waybillService.getForAssignee(companyId, principal.userId(), waybillId);
    }

    @GetMapping("/{waybillId}/status-history")
    @Operation(summary = "List the status history of a waybill assigned to me",
            description = "Returns the paginated status-change history (newest first) of a waybill assigned to the current web user. Each "
                    + "entry records the transition (fromStatus/toStatus), the account that performed it, the instant of the change "
                    + "(ISO-8601 UTC) and the optional note and GPS coordinates captured at that moment. Returns 403 if the waybill is not "
                    + "assigned to this user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of status-history entries",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<StatusHistoryResponse> statusHistory(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return waybillService.getStatusHistoryForAssignee(companyId, principal.userId(), waybillId, pageable);
    }

    @PostMapping("/{waybillId}/status")
    @Operation(summary = "Change the status of a waybill assigned to me",
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
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse changeStatus(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @Valid @RequestBody ChangeWaybillStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return waybillService.changeStatusForAssignee(companyId, principal.userId(), waybillId, request);
    }

    @PostMapping("/{waybillId}/parcels/{parcelId}/status")
    @Operation(summary = "Change the status of a parcel of a waybill assigned to me",
            description = "Sets a single parcel (goods line) of a waybill assigned to the current web user to any status (PENDING, LOADED, "
                    + "IN_TRANSIT, DELIVERED, FAILED, CANCELLED). No lifecycle restriction: any status from any other, including backward. "
                    + "Setting the same status is a no-op. Every effective change is recorded in the parcel status history. Returns the full "
                    + "waybill.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parcel status changed",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parcel not found in this waybill",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse changeParcelStatus(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @Parameter(description = "Identifier of the parcel (goods line) within the waybill")
            @PathVariable UUID parcelId,
            @Valid @RequestBody ChangeParcelStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return waybillService.changeParcelStatusForAssignee(companyId, principal.userId(), waybillId, parcelId, request);
    }

    @GetMapping("/{waybillId}/parcels/{parcelId}/status-history")
    @Operation(summary = "List the status history of a parcel of a waybill assigned to me",
            description = "Returns the paginated status-change history (newest first) of a single parcel (goods line) of a waybill assigned "
                    + "to the current web user. Returns 403 if the waybill is not assigned to this user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of status-history entries",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parcel not found in this waybill",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<StatusHistoryResponse> parcelStatusHistory(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @PathVariable UUID parcelId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return waybillService.getParcelStatusHistoryForAssignee(companyId, principal.userId(), waybillId, parcelId, pageable);
    }

    @PostMapping("/{waybillId}/signatures")
    @Operation(summary = "Capture an eCMR signature on a waybill assigned to me",
            description = "Captures the recipient's electronic signature (eCMR) on a waybill assigned to the current web user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Signature captured",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WaybillResponse> addSignature(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @Valid @RequestBody SignatureDto request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        WaybillResponse response = waybillService.addSignatureForAssignee(companyId, principal.userId(), waybillId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{waybillId}/proof", consumes = "multipart/form-data")
    @Operation(summary = "Upload a proof of delivery on a waybill assigned to me",
            description = "Uploads a proof-of-delivery photo for a waybill assigned to the current web user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proof uploaded",
                    content = @Content(schema = @Schema(implementation = WaybillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing or unsupported image",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Waybill not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WaybillResponse uploadProof(
            @PathVariable UUID companyId,
            @PathVariable UUID waybillId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return waybillService.addProofForAssignee(companyId, principal.userId(), waybillId, file);
    }
}

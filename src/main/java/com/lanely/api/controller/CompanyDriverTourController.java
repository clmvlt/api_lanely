package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.common.StatusHistoryResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.tour.ChangeTourStatusRequest;
import com.lanely.api.dto.tour.TourResponse;
import com.lanely.api.dto.tour.TourSummaryResponse;
import com.lanely.api.entity.enums.TourStatus;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.CompanyService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/assignments/tours")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Driver (Web user) · Tours",
        description = "Mobile endpoints for a web user, acting as a driver in a given company, to handle the tours assigned to it. "
                + "Mirror of the Profile · Tours endpoints, scoped by companyId because a web user may belong to several companies.")
public class CompanyDriverTourController {

    private final TourService tourService;
    private final CompanyService companyService;

    public CompanyDriverTourController(TourService tourService, CompanyService companyService) {
        this.tourService = tourService;
        this.companyService = companyService;
    }

    @GetMapping
    @Operation(summary = "List the tours assigned to me in this company",
            description = "Returns the tours assigned to the current web user within the given company. The caller must be a member of "
                    + "the company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of tours",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of the company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<TourSummaryResponse> list(
            @PathVariable UUID companyId,
            @Parameter(description = "Filter by status", example = "ASSIGNED")
            @RequestParam(required = false) TourStatus status,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return tourService.listForAssignee(companyId, principal.userId(), status, pageable);
    }

    @GetMapping("/{tourId}")
    @Operation(summary = "Get one of the tours assigned to me in this company",
            description = "Returns a tour assigned to the current web user, with its ordered waybills and route. Returns 403 if the tour "
                    + "is not assigned to this user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tour returned",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tour not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse get(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return tourService.getForAssignee(companyId, principal.userId(), tourId);
    }

    @PostMapping("/{tourId}/status")
    @Operation(summary = "Change the status of a tour assigned to me",
            description = "Sets the tour to any status (PLANNED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED). There is no lifecycle "
                    + "restriction: any status can be set from any other, including moving backward or out of a terminal status, to allow "
                    + "corrections from the field. Setting the same status is a no-op. Reaching IN_PROGRESS/COMPLETED sets the matching "
                    + "timestamps. Every effective change is recorded in the status history.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tour not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse changeStatus(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @Valid @RequestBody ChangeTourStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return tourService.changeStatusForAssignee(companyId, principal.userId(), tourId, request);
    }

    @GetMapping("/{tourId}/status-history")
    @Operation(summary = "List the status history of a tour assigned to me",
            description = "Returns the paginated status-change history (newest first) of a tour assigned to the current web user. Returns "
                    + "403 if the tour is not assigned to this user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of status-history entries",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tour not assigned to this user (or caller not a member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<StatusHistoryResponse> statusHistory(
            @PathVariable UUID companyId,
            @PathVariable UUID tourId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        companyService.requireMember(companyId, principal.userId());
        return tourService.getStatusHistoryForAssignee(companyId, principal.userId(), tourId, pageable);
    }
}

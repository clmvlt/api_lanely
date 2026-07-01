package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.common.StatusHistoryResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.tour.ChangeTourStatusRequest;
import com.lanely.api.dto.tour.TourResponse;
import com.lanely.api.dto.tour.TourSummaryResponse;
import com.lanely.api.entity.enums.TourStatus;
import com.lanely.api.security.AuthenticatedProfile;
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
@RequestMapping("/profile/tours")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Profile · Tours", description = "Mobile app endpoints for a driver profile to handle the tours assigned to it")
public class ProfileTourController {

    private final TourService tourService;

    public ProfileTourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping
    @Operation(summary = "List my tours",
            description = "Returns the tours assigned to the current driver profile. Reserved to profile (mobile) accounts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of tours",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a profile account",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<TourSummaryResponse> list(
            @Parameter(description = "Filter by status", example = "ASSIGNED")
            @RequestParam(required = false) TourStatus status,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return tourService.listForAssignee(principal.companyId(), principal.profileId(), status, pageable);
    }

    @GetMapping("/{tourId}")
    @Operation(summary = "Get one of my tours",
            description = "Returns a tour assigned to the current profile, with its ordered waybills and route. Returns 403 otherwise.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tour returned",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tour not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse get(
            @PathVariable UUID tourId,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return tourService.getForAssignee(principal.companyId(), principal.profileId(), tourId);
    }

    @PostMapping("/{tourId}/status")
    @Operation(summary = "Change a tour status",
            description = "Sets the tour to any status (PLANNED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED). There is no lifecycle "
                    + "restriction: any status can be set from any other, including moving backward or out of a terminal status, to allow "
                    + "corrections from the field. Setting the same status is a no-op. Reaching IN_PROGRESS/COMPLETED sets the matching "
                    + "timestamps. Every effective change is recorded in the status history.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tour not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TourResponse changeStatus(
            @PathVariable UUID tourId,
            @Valid @RequestBody ChangeTourStatusRequest request,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return tourService.changeStatusForAssignee(principal.companyId(), principal.profileId(), tourId, request);
    }

    @GetMapping("/{tourId}/status-history")
    @Operation(summary = "List the status history of one of my tours",
            description = "Returns the paginated status-change history (newest first) of a tour assigned to the current profile. Returns 403 "
                    + "if the tour is not assigned to this profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of status-history entries",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tour not assigned to this profile",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<StatusHistoryResponse> statusHistory(
            @PathVariable UUID tourId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedProfile principal) {
        return tourService.getStatusHistoryForAssignee(principal.companyId(), principal.profileId(), tourId, pageable);
    }
}

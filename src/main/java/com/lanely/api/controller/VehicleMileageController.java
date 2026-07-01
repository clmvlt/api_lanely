package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.vehicle.CreateMileageReadingRequest;
import com.lanely.api.dto.vehicle.MileageReadingResponse;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.VehicleService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/vehicles/{vehicleId}/mileage-readings")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Vehicle mileage", description = "Record and read the mileage (odometer) history of a vehicle")
public class VehicleMileageController {

    private final VehicleService vehicleService;

    public VehicleMileageController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @Operation(
            summary = "Add a mileage reading",
            description = "Records a mileage (odometer) reading for the vehicle, with the value in kilometers and the instant it was taken. "
                    + "Any member of the company can add a reading (no special permission required). The vehicle's latest mileage is updated "
                    + "when the new reading is the most recent one by recordedAt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mileage reading added",
                    content = @Content(schema = @Schema(implementation = MileageReadingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MileageReadingResponse> addMileageReading(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateMileageReadingRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        MileageReadingResponse response = vehicleService.addMileageReading(principal.userId(), companyId, vehicleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List mileage readings",
            description = "Returns a paginated list of the vehicle's mileage readings, most recent first (by recordedAt). The caller must be "
                    + "a member of the company. Pagination uses the standard page and size query parameters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of mileage readings returned",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<MileageReadingResponse> listMileageReadings(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return vehicleService.listMileageReadings(principal.userId(), companyId, vehicleId, pageable);
    }
}

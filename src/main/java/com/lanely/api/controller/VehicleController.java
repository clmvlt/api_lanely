package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.vehicle.CreateVehicleRequest;
import com.lanely.api.dto.vehicle.UpdateVehicleRequest;
import com.lanely.api.dto.vehicle.VehicleResponse;
import com.lanely.api.dto.vehicle.VehicleSummaryResponse;
import com.lanely.api.entity.enums.VehicleStatus;
import com.lanely.api.entity.enums.VehicleType;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/vehicles")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Vehicles", description = "Manage the vehicles (fleet) of a transport company, including their documents and mileage history")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @Operation(
            summary = "Create a vehicle",
            description = "Creates a vehicle in the company's fleet. Requires the MANAGE_VEHICLES permission (the OWNER always has it). "
                    + "The registration plate is unique within the company. Only registrationPlate and vehicleType are required; all other "
                    + "fields (identification, insurance, weights, regulatory dates) are optional. Documents/photos and mileage readings are "
                    + "managed through their own sub-resources."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehicle created",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_VEHICLES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "A vehicle with the same registration plate already exists in this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> createVehicle(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateVehicleRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        VehicleResponse response = vehicleService.createVehicle(principal.userId(), companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List vehicles",
            description = "Returns a paginated list of the company's vehicles. The caller must be a member of the company. "
                    + "Optionally filter by status, by vehicle type and by a free-text query matching the plate, make, model or VIN. "
                    + "Pagination uses the standard page, size and sort query parameters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of vehicles returned",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<VehicleSummaryResponse> listVehicles(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Filter by lifecycle status. Omit to return vehicles of any status.", example = "ACTIVE")
            @RequestParam(required = false) VehicleStatus status,
            @Parameter(description = "Filter by vehicle category. Omit to return vehicles of any type.", example = "TRUCK")
            @RequestParam(required = false) VehicleType type,
            @Parameter(description = "Free-text search on plate, make, model or VIN (case-insensitive)", example = "renault")
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return vehicleService.listVehicles(principal.userId(), companyId, status, type, q, pageable);
    }

    @GetMapping("/{vehicleId}")
    @Operation(
            summary = "Get a vehicle",
            description = "Returns the full detail of a vehicle, including its attached documents and its latest recorded mileage. "
                    + "The caller must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle returned",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public VehicleResponse getVehicle(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return vehicleService.getVehicle(principal.userId(), companyId, vehicleId);
    }

    @PatchMapping("/{vehicleId}")
    @Operation(
            summary = "Update a vehicle",
            description = "Updates a vehicle's editable information. Only non-null fields are applied. Requires the MANAGE_VEHICLES permission. "
                    + "A changed registration plate must remain unique within the company. When the insurance block is provided it replaces "
                    + "the stored insurance details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle updated",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_VEHICLES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Another vehicle already uses the new registration plate",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public VehicleResponse updateVehicle(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @Valid @RequestBody UpdateVehicleRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return vehicleService.updateVehicle(principal.userId(), companyId, vehicleId, request);
    }

    @PostMapping("/{vehicleId}/archive")
    @Operation(
            summary = "Archive a vehicle",
            description = "Moves a vehicle to the ARCHIVED status. It is kept for history but hidden from the default active list. "
                    + "Requires the MANAGE_VEHICLES permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle archived",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_VEHICLES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public VehicleResponse archiveVehicle(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return vehicleService.setVehicleStatus(principal.userId(), companyId, vehicleId, VehicleStatus.ARCHIVED);
    }

    @PostMapping("/{vehicleId}/restore")
    @Operation(
            summary = "Restore a vehicle",
            description = "Moves an archived vehicle back to the ACTIVE status. Requires the MANAGE_VEHICLES permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle restored",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_VEHICLES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public VehicleResponse restoreVehicle(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return vehicleService.setVehicleStatus(principal.userId(), companyId, vehicleId, VehicleStatus.ACTIVE);
    }

    @DeleteMapping("/{vehicleId}")
    @Operation(
            summary = "Delete a vehicle",
            description = "Permanently deletes a vehicle, along with its attached documents (files and stored content) and its mileage history. "
                    + "Requires the MANAGE_VEHICLES permission. Prefer archiving when the vehicle should be kept for history."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vehicle deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_VEHICLES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        vehicleService.deleteVehicle(principal.userId(), companyId, vehicleId);
        return ResponseEntity.noContent().build();
    }
}

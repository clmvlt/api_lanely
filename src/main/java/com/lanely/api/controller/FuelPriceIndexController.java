package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.fuel.FuelPriceResponse;
import com.lanely.api.dto.fuel.RefreshFuelPricesResponse;
import com.lanely.api.entity.enums.FuelType;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.FuelPriceService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/fuel-prices")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fuel prices", description = "Read government fuel price observations and trigger a refresh, used by the fuel surcharge indexation")
public class FuelPriceIndexController {

    private final FuelPriceService fuelPriceService;

    public FuelPriceIndexController(FuelPriceService fuelPriceService) {
        this.fuelPriceService = fuelPriceService;
    }

    @GetMapping("/current")
    @Operation(
            summary = "Get the current fuel price",
            description = "Returns the most recent stored fuel price observation for the given fuel type (DIESEL by default). "
                    + "The caller must be a member of the company. Optionally pin the lookup to a specific data source."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Latest fuel price returned",
                    content = @Content(schema = @Schema(implementation = FuelPriceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found, or no fuel index available for the fuel type",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public FuelPriceResponse current(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Fuel type to look up. Defaults to DIESEL.", example = "DIESEL")
            @RequestParam(required = false) FuelType fuelType,
            @Parameter(description = "Restrict to a specific data source. Omit to use the latest from any source.", example = "data.economie.gouv.fr")
            @RequestParam(required = false) String source,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return fuelPriceService.current(principal.userId(), companyId, fuelType, source);
    }

    @GetMapping("/history")
    @Operation(
            summary = "List fuel price history",
            description = "Returns a paginated time series of stored fuel price observations for the given fuel type (DIESEL by default), "
                    + "optionally filtered by source and a reference-date range. The caller must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of fuel prices returned",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<FuelPriceResponse> history(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Fuel type to look up. Defaults to DIESEL.", example = "DIESEL")
            @RequestParam(required = false) FuelType fuelType,
            @Parameter(description = "Restrict to a specific data source", example = "data.economie.gouv.fr")
            @RequestParam(required = false) String source,
            @Parameter(description = "Lower bound of the reference date (inclusive, civil date)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Upper bound of the reference date (inclusive, civil date)", example = "2026-06-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return fuelPriceService.history(principal.userId(), companyId, fuelType, source, from, to, pageable);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh fuel prices from the government feed",
            description = "Fetches the configured government dataset and upserts the observations it contains (idempotent by fuel type, "
                    + "reference date and source). Requires the MANAGE_PRICING permission. Returns how many observations were created or updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refresh completed",
                    content = @Content(schema = @Schema(implementation = RefreshFuelPricesResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PRICING permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "The government fuel price API returned an error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "The government fuel price API is unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RefreshFuelPricesResponse refresh(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return fuelPriceService.refresh(principal.userId(), companyId);
    }
}

package com.lanely.api.controller;

import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.pricing.QuoteRequest;
import com.lanely.api.dto.pricing.QuoteResponse;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/pricing")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pricing", description = "Estimate delivery prices from a company's rate cards, with the full breakdown. All monetary amounts are tax-excluded (HT).")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/quote")
    @Operation(
            summary = "Estimate a price (quote)",
            description = "Computes a price quote without persisting anything. The caller must be a member of the company. "
                    + "Provide either a waybillId (quantities and client are read from the waybill) or ad-hoc inputs (distance, weight, volume, "
                    + "packages, stops). The rate card is resolved in this order: explicit tariffId, then a client-specific ACTIVE grid valid "
                    + "today, then the company ACTIVE default grid. The response returns the full breakdown (base lines then fuel surcharge lines), "
                    + "the subtotal, surcharge total and final total, the fuel price used, and any non-fatal warnings (e.g. route distance not "
                    + "computed, no fuel index available). All amounts in the quote are tax-excluded (HT)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quote computed",
                    content = @Content(schema = @Schema(implementation = QuoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "No applicable rate card could be resolved",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, waybill or forced rate card not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public QuoteResponse quote(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody QuoteRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return pricingService.quote(principal.userId(), companyId, request);
    }
}

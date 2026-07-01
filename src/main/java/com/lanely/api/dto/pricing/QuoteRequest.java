package com.lanely.api.dto.pricing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.UUID;

@Schema(name = "QuoteRequest", description = "Request to estimate a price. Either reference an existing waybill, or provide ad-hoc inputs. "
        + "When waybillId is set, its client and quantities are used and the other fields are ignored except an explicit tariffId override.")
public record QuoteRequest(

        @Schema(description = "Existing waybill to price. When set, quantities and client are read from it.", example = "5d2c1b3a-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID waybillId,

        @Schema(description = "Force a specific rate card. When null, the grid is resolved from the client then the company default.", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID tariffId,

        @Schema(description = "Client used to resolve a client-specific grid (ignored when waybillId is set)", example = "7a1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID clientId,

        @Schema(description = "Ad-hoc quantities (used when waybillId is null)", nullable = true)
        @Valid
        QuoteInputsDto inputs
) {
}

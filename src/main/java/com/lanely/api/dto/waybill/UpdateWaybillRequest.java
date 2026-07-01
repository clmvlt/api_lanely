package com.lanely.api.dto.waybill;

import com.lanely.api.entity.enums.WaybillScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(name = "UpdateWaybillRequest", description = "Update an editable waybill (DRAFT or ISSUED). Only non-null fields are applied; "
        + "when a parties or goods block is provided it replaces the existing one.")
public record UpdateWaybillRequest(

        @Schema(description = "New reference (must remain unique within the company)", example = "WBL-0002", nullable = true)
        @Size(max = 32)
        String reference,

        @Schema(description = "National or international (CMR) transport", example = "INTERNATIONAL", nullable = true)
        WaybillScope scope,

        @Schema(description = "Ordering customer (donneur d'ordre). When provided, replaces the current one; it cannot be cleared.",
                example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID clientId,

        @Schema(description = "Shipper / sender", nullable = true)
        @Valid
        WaybillPartyDto shipper,

        @Schema(description = "Consignee / recipient", nullable = true)
        @Valid
        WaybillPartyDto consignee,

        @Schema(description = "Place of taking over", nullable = true)
        @Valid
        PlaceDto placeOfTakingOver,

        @Schema(description = "Place of delivery", nullable = true)
        @Valid
        PlaceDto placeOfDelivery,

        @Schema(description = "Lines of goods (replaces the existing list when provided)", nullable = true)
        @Valid
        List<GoodsLineDto> goodsLines,

        @Schema(description = "Documents attached", nullable = true)
        String attachedDocuments,

        @Schema(description = "Sender's instructions", nullable = true)
        String senderInstructions,

        @Schema(description = "Carriage charges amount (borne by the ordering customer), tax-excluded (HT)", nullable = true)
        BigDecimal carriageChargesAmount,

        @Schema(description = "ISO 4217 currency code", nullable = true)
        @Size(max = 3)
        String carriageChargesCurrency,

        @Schema(description = "Reservations and observations", nullable = true)
        String reservationsAndObservations,

        @Schema(description = "Pre-computed round-trip route (distance, duration, geometry). "
                + "When provided, it is stored as-is and the API does NOT compute the route; "
                + "when omitted, the API recomputes it from the depot and place coordinates.", nullable = true)
        @Valid
        RouteInputDto route,

        @Schema(description = "Free-form internal notes", nullable = true)
        String notes
) {
}

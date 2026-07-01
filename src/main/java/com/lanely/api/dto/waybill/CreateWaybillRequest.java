package com.lanely.api.dto.waybill;

import com.lanely.api.entity.enums.WaybillScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(name = "CreateWaybillRequest", description = "Payload to create a waybill (consignment note). Created in DRAFT status.")
public record CreateWaybillRequest(

        @Schema(description = "Human-friendly reference, unique within the company. Auto-generated (e.g. WBL-0001) when omitted.",
                example = "WBL-0001", nullable = true)
        @Size(max = 32)
        String reference,

        @Schema(description = "National or international (CMR) transport", example = "NATIONAL", defaultValue = "NATIONAL", nullable = true)
        WaybillScope scope,

        @Schema(description = "Ordering customer (donneur d'ordre): the client on whose behalf the transport is carried out. Mandatory.",
                example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID clientId,

        @Schema(description = "Shipper / sender of the goods", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Valid
        WaybillPartyDto shipper,

        @Schema(description = "Consignee / recipient of the goods", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Valid
        WaybillPartyDto consignee,

        @Schema(description = "Place and planned time of taking over the goods", nullable = true)
        @Valid
        PlaceDto placeOfTakingOver,

        @Schema(description = "Place and planned time of delivery", nullable = true)
        @Valid
        PlaceDto placeOfDelivery,

        @Schema(description = "Lines of goods carried", nullable = true)
        @Valid
        List<GoodsLineDto> goodsLines,

        @Schema(description = "Documents attached to the consignment note", nullable = true)
        String attachedDocuments,

        @Schema(description = "Sender's instructions / special conditions", nullable = true)
        String senderInstructions,

        @Schema(description = "Carriage charges amount (borne by the ordering customer), tax-excluded (HT)", example = "450.00", nullable = true)
        BigDecimal carriageChargesAmount,

        @Schema(description = "ISO 4217 currency code of the carriage charges", example = "EUR", nullable = true)
        @Size(max = 3)
        String carriageChargesCurrency,

        @Schema(description = "Reservations and observations of the carrier", nullable = true)
        String reservationsAndObservations,

        @Schema(description = "Tour to attach the waybill to (optional)", nullable = true)
        UUID tourId,

        @Schema(description = "Account to assign directly as the driver (optional). May be a mobile driver profile of the "
                + "company or any web user that is a member of the company (owners are members too).", nullable = true)
        UUID assignedAccountId,

        @Schema(description = "Pre-computed round-trip route (distance, duration, geometry). "
                + "When provided, it is stored as-is and the API does NOT compute the route; "
                + "when omitted, the API computes it from the depot and place coordinates.", nullable = true)
        @Valid
        RouteInputDto route,

        @Schema(description = "Free-form internal notes", nullable = true)
        String notes
) {
}

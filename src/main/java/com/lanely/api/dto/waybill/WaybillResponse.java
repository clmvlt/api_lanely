package com.lanely.api.dto.waybill;

import com.lanely.api.dto.geo.RouteInfoDto;
import com.lanely.api.entity.enums.AccountType;
import com.lanely.api.entity.enums.WaybillScope;
import com.lanely.api.entity.enums.WaybillStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "WaybillResponse", description = "A waybill (consignment note) with its parties, goods, signatures and route")
public record WaybillResponse(

        @Schema(description = "Waybill identifier", example = "0a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d")
        UUID id,

        @Schema(description = "Reference, unique within the company", example = "WBL-0001")
        String reference,

        @Schema(description = "Lifecycle status", example = "DRAFT")
        WaybillStatus status,

        @Schema(description = "National or international (CMR)", example = "NATIONAL")
        WaybillScope scope,

        @Schema(description = "Ordering customer (donneur d'ordre) identifier", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID clientId,

        @Schema(description = "Ordering customer (donneur d'ordre) display name", example = "ACME Retail")
        String clientName,

        @Schema(description = "Shipper / sender", nullable = true)
        WaybillPartyResponse shipper,

        @Schema(description = "Consignee / recipient", nullable = true)
        WaybillPartyResponse consignee,

        @Schema(description = "Place of taking over", nullable = true)
        PlaceResponse placeOfTakingOver,

        @Schema(description = "Place of delivery", nullable = true)
        PlaceResponse placeOfDelivery,

        @Schema(description = "Lines of goods")
        List<GoodsLineResponse> goodsLines,

        @Schema(description = "Electronic signatures")
        List<SignatureResponse> signatures,

        @Schema(description = "Documents attached", nullable = true)
        String attachedDocuments,

        @Schema(description = "Sender's instructions", nullable = true)
        String senderInstructions,

        @Schema(description = "Carriage charges amount (borne by the ordering customer), tax-excluded (HT)", nullable = true)
        BigDecimal carriageChargesAmount,

        @Schema(description = "Currency code", nullable = true)
        String carriageChargesCurrency,

        @Schema(description = "Reservations and observations", nullable = true)
        String reservationsAndObservations,

        @Schema(description = "Tour the waybill belongs to, if any", nullable = true)
        UUID tourId,

        @Schema(description = "Position within the tour, if any", nullable = true)
        Integer positionInTour,

        @Schema(description = "Driver account assigned directly, if any. May be a mobile driver profile or a web user.", nullable = true)
        UUID assignedAccountId,

        @Schema(description = "Type of the directly assigned driver account (PROFILE or USER), if any", example = "PROFILE", nullable = true)
        AccountType assigneeType,

        @Schema(description = "Display name of the directly assigned driver, if any", example = "John D.", nullable = true)
        String assigneeName,

        @Schema(description = "Cached round-trip route: company depot -> shipper (pickup) -> consignee (delivery) -> company depot. "
                + "Recomputed automatically on create/update.", nullable = true)
        RouteInfoDto route,

        @Schema(description = "Reason of a failed delivery", nullable = true)
        String failureReason,

        @Schema(description = "Relative URL of the proof-of-delivery photo, if any", nullable = true)
        String proofOfDeliveryImageUrl,

        @Schema(description = "Free-form internal notes", nullable = true)
        String notes,

        @Schema(description = "Whether the waybill is archived. An archived waybill is hidden from default lists but stays readable; "
                + "archiving is independent of the business status (a waybill of any status can be archived).", example = "false")
        boolean archived,

        @Schema(type = "string", format = "date-time", description = "Instant the waybill was archived (ISO-8601 UTC), null when not archived",
                example = "2026-06-10T09:15:30Z", nullable = true)
        Instant archivedAt,

        @Schema(type = "string", format = "date-time",
                description = "Instant the waybill last entered the dock (ISO-8601 UTC), null when it never went to the dock. "
                        + "Set when status moves to AT_DOCK; dwell time at dock is derived client-side as now - dockEnteredAt while still AT_DOCK.",
                example = "2026-06-10T09:15:30Z", nullable = true)
        Instant dockEnteredAt,

        @Schema(type = "string", format = "date-time",
                description = "Instant the waybill last left the dock (ISO-8601 UTC), null while still at dock or never docked",
                example = "2026-06-17T07:30:00Z", nullable = true)
        Instant dockExitedAt,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt,

        @Schema(type = "string", format = "date-time", description = "Last update instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant updatedAt
) {
}

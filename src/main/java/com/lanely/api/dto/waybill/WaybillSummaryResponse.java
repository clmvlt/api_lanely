package com.lanely.api.dto.waybill;

import com.lanely.api.entity.enums.AccountType;
import com.lanely.api.entity.enums.WaybillScope;
import com.lanely.api.entity.enums.WaybillStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "WaybillSummaryResponse", description = "Compact view of a waybill for lists")
public record WaybillSummaryResponse(

        @Schema(description = "Waybill identifier", example = "0a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d")
        UUID id,

        @Schema(description = "Reference", example = "WBL-0001")
        String reference,

        @Schema(description = "Lifecycle status", example = "DRAFT")
        WaybillStatus status,

        @Schema(description = "National or international", example = "NATIONAL")
        WaybillScope scope,

        @Schema(description = "Ordering customer (donneur d'ordre) name", example = "ACME Retail")
        String clientName,

        @Schema(description = "Shipper name", example = "ACME Logistics", nullable = true)
        String shipperName,

        @Schema(description = "Consignee name", example = "Jean Martin", nullable = true)
        String consigneeName,

        @Schema(description = "Place of taking over (pickup) city", example = "Paris", nullable = true)
        String pickupCity,

        @Schema(description = "Place of taking over (pickup) latitude in decimal degrees (WGS84)", example = "48.8566", nullable = true)
        Double pickupLatitude,

        @Schema(description = "Place of taking over (pickup) longitude in decimal degrees (WGS84)", example = "2.3522", nullable = true)
        Double pickupLongitude,

        @Schema(type = "string", format = "date-time", description = "Planned pickup instant (ISO-8601 UTC)", example = "2026-06-23T08:00:00Z", nullable = true)
        Instant pickupPlannedAt,

        @Schema(description = "Delivery city", example = "Rennes", nullable = true)
        String deliveryCity,

        @Schema(description = "Place of delivery latitude in decimal degrees (WGS84)", example = "48.1173", nullable = true)
        Double deliveryLatitude,

        @Schema(description = "Place of delivery longitude in decimal degrees (WGS84)", example = "-1.6778", nullable = true)
        Double deliveryLongitude,

        @Schema(type = "string", format = "date-time", description = "Planned delivery instant (ISO-8601 UTC)", example = "2026-06-23T16:00:00Z", nullable = true)
        Instant deliveryPlannedAt,

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

        @Schema(description = "Whether the waybill is archived (hidden from default lists, independent of the business status)", example = "false")
        boolean archived,

        @Schema(type = "string", format = "date-time", description = "Instant the waybill was archived (ISO-8601 UTC), null when not archived",
                example = "2026-06-10T09:15:30Z", nullable = true)
        Instant archivedAt,

        @Schema(type = "string", format = "date-time",
                description = "Instant the waybill last entered the dock (ISO-8601 UTC), null when it never went to the dock. "
                        + "Dwell time at dock is derived client-side as now - dockEnteredAt while status is AT_DOCK.",
                example = "2026-06-10T09:15:30Z", nullable = true)
        Instant dockEnteredAt,

        @Schema(type = "string", format = "date-time",
                description = "Instant the waybill last left the dock (ISO-8601 UTC), null while still at dock or never docked",
                example = "2026-06-17T07:30:00Z", nullable = true)
        Instant dockExitedAt,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt
) {
}

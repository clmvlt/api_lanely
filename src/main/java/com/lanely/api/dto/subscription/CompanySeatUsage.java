package com.lanely.api.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "CompanySeatUsage", description = "Seat consumption of a single company. A seat is one active profile or one member; "
        + "deactivated profiles do not consume a seat.")
public record CompanySeatUsage(

        @Schema(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
        UUID companyId,

        @Schema(description = "Display name of the company", example = "Speedy Delivery")
        String companyName,

        @Schema(description = "Number of active mobile profiles in the company", example = "5")
        long activeProfiles,

        @Schema(description = "Number of web members in the company (owner included)", example = "2")
        long members,

        @Schema(description = "Total seats currently used (activeProfiles + members)", example = "7")
        long seatsUsed,

        @Schema(description = "Maximum seats allowed for this company, derived from the owner's plan (0 if the owner has no plan)", example = "10")
        int seatsLimit,

        @Schema(description = "Seats still available before reaching the limit", example = "3")
        long seatsRemaining
) {
}

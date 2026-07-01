package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "AssignWaybillRequest", description = "Assign a waybill to a tour and/or directly to a driver. "
        + "A null field clears the corresponding assignment.")
public record AssignWaybillRequest(

        @Schema(description = "Tour to attach the waybill to (null detaches it)", example = "2b3c4d5e-6f70-8a9b-0c1d-2e3f4a5b6c7d", nullable = true)
        UUID tourId,

        @Schema(description = "Position within the tour (0-based). Defaults to the end when omitted.", example = "2", nullable = true)
        Integer positionInTour,

        @Schema(description = "Account to assign directly as the driver (null clears the direct assignment). May be either a "
                + "mobile driver profile of the company or any web user that is a member of the company (owners are members too).",
                example = "3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f", nullable = true)
        UUID assignedAccountId
) {
}

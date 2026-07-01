package com.lanely.api.dto.tour;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "AssignTourRequest", description = "Assign a vehicle and/or a driver to a tour. A null field clears the corresponding assignment.")
public record AssignTourRequest(

        @Schema(description = "Vehicle to assign (null clears it)", example = "4d5e6f70-8a9b-0c1d-2e3f-4a5b6c7d8e9f", nullable = true)
        UUID vehicleId,

        @Schema(description = "Account to assign as the driver (null clears it). May be either a mobile driver profile "
                + "of the company or any web user that is a member of the company (owners are members too).",
                example = "5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b", nullable = true)
        UUID assignedAccountId
) {
}

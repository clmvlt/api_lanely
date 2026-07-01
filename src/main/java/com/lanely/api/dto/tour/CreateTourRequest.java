package com.lanely.api.dto.tour;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.dto.geo.CoordinateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "CreateTourRequest", description = "Payload to create a tour (round). Created in PLANNED status.")
public record CreateTourRequest(

        @Schema(description = "Human-friendly reference, unique within the company. Auto-generated (e.g. TUR-0001) when omitted.",
                example = "TUR-0001", nullable = true)
        @Size(max = 32)
        String reference,

        @Schema(description = "Display name of the tour", example = "Rennes morning round", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(type = "string", format = "date", description = "Planned date of the tour (ISO-8601)", example = "2026-06-24", nullable = true)
        LocalDate plannedDate,

        @Schema(description = "Depot postal address (start and end point). Defaults to the company billing address when omitted.", nullable = true)
        @Valid
        AddressDto depot,

        @Schema(description = "Depot GPS coordinates (used as the optimization start/end). Server geocodes the depot address when omitted.", nullable = true)
        @Valid
        CoordinateDto depotLocation,

        @Schema(description = "Vehicle to assign (optional)", nullable = true)
        UUID vehicleId,

        @Schema(description = "Account to assign as the driver (optional). May be a mobile driver profile of the company or "
                + "any web user that is a member of the company (owners are members too). Assigning one moves the tour to ASSIGNED.",
                nullable = true)
        UUID assignedAccountId,

        @Schema(description = "Free-form internal notes", nullable = true)
        String notes
) {
}

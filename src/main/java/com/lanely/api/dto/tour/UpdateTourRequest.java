package com.lanely.api.dto.tour;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.dto.geo.CoordinateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "UpdateTourRequest", description = "Update a tour. Only non-null fields are applied.")
public record UpdateTourRequest(

        @Schema(description = "Display name", example = "Rennes afternoon round", nullable = true)
        @Size(max = 150)
        String name,

        @Schema(type = "string", format = "date", description = "Planned date (ISO-8601)", example = "2026-06-24", nullable = true)
        LocalDate plannedDate,

        @Schema(description = "Depot postal address", nullable = true)
        @Valid
        AddressDto depot,

        @Schema(description = "Depot GPS coordinates", nullable = true)
        @Valid
        CoordinateDto depotLocation,

        @Schema(description = "Free-form internal notes", nullable = true)
        String notes
) {
}

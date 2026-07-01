package com.lanely.api.dto.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "InsuranceInfoDto", description = "Insurance details of a vehicle. All fields are optional.")
public record InsuranceInfoDto(

        @Schema(description = "Name of the insurer", example = "AXA Fleet", nullable = true)
        @Size(max = 200)
        String insurerName,

        @Schema(description = "Insurance policy (contract) number", example = "POL-2026-00198", nullable = true)
        @Size(max = 64)
        String policyNumber,

        @Schema(description = "Type of coverage (e.g. comprehensive, third-party)", example = "comprehensive", nullable = true)
        @Size(max = 64)
        String coverageType,

        @Schema(type = "string", format = "date", description = "Coverage start date (ISO-8601, civil date)", example = "2026-01-01", nullable = true)
        LocalDate startDate,

        @Schema(type = "string", format = "date", description = "Coverage end date (ISO-8601, civil date)", example = "2026-12-31", nullable = true)
        LocalDate endDate,

        @Schema(description = "Contact for the insurance (agent name, phone or e-mail)", example = "+33123456789", nullable = true)
        @Size(max = 200)
        String contact
) {
}

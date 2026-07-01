package com.lanely.api.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "LegalInfoDto", description = "Legal identification of the company, used for invoicing. All fields are optional.")
public record LegalInfoDto(

        @Schema(description = "Registered legal name (raison sociale), if different from the display name", example = "Speedy Delivery SAS", nullable = true)
        @Size(max = 200)
        String legalName,

        @Schema(description = "Company registration number (e.g. SIREN/SIRET in France, company number elsewhere)", example = "552100554", nullable = true)
        @Size(max = 64)
        String registrationNumber,

        @Schema(description = "Intra-community or national VAT number", example = "FR40552100554", nullable = true)
        @Size(max = 32)
        String vatNumber,

        @Schema(description = "Legal form (e.g. SAS, SARL, SA, sole proprietorship)", example = "SAS", nullable = true)
        @Size(max = 64)
        String legalForm
) {
}

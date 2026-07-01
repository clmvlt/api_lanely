package com.lanely.api.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "CompanyCodeResponse", description = "The public code of a company, shared with mobile profiles")
public record CompanyCodeResponse(

        @Schema(description = "Unique company identifier", example = "11112222-3333-4444-5555-666677778888")
        UUID companyId,

        @Schema(description = "Public company code", example = "K7P2M9QX")
        String publicCode
) {
}

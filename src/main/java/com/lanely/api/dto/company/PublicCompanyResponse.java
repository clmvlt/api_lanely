package com.lanely.api.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "PublicCompanyResponse", description = "Non-sensitive company information resolved from a public code (mobile scan step)")
public record PublicCompanyResponse(

        @Schema(description = "Unique company identifier", example = "11112222-3333-4444-5555-666677778888")
        UUID id,

        @Schema(description = "Company display name", example = "Speedy Delivery")
        String name,

        @Schema(description = "Public company code", example = "K7P2M9QX")
        String publicCode,

        @Schema(description = "Relative URL of the company profile picture, or null if none", example = "/images/aaaa1111-bbbb-2222-cccc-3333dddd4444", nullable = true)
        String profileImageUrl
) {
}

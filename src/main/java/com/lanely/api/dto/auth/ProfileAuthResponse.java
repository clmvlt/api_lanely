package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "ProfileAuthResponse", description = "Authentication result for a mobile delivery session: tokens plus the driver identity. "
        + "The driver can be a standalone delivery profile (subjectType=PROFILE) or a web user acting as a driver for the company "
        + "(subjectType=DRIVER); in both cases the session grants the same driver access, scoped to the company.")
public record ProfileAuthResponse(

        @Schema(description = "Access and refresh tokens for the new session")
        TokenResponse tokens,

        @Schema(description = "Identifier of the authenticated account driving this session: a Profile id when subjectType=PROFILE, "
                + "a User id when subjectType=DRIVER. This is the id tours are assigned to.", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e")
        UUID profileId,

        @Schema(description = "Identifier of the company this driver session is scoped to", example = "11112222-3333-4444-5555-666677778888")
        UUID companyId,

        @Schema(description = "Display name of the driver: the profile username when subjectType=PROFILE, the user's full name "
                + "(or email) when subjectType=DRIVER", example = "driver01")
        String username,

        @Schema(description = "Kind of account behind this driver session: PROFILE (a standalone delivery profile) or DRIVER "
                + "(a web user acting as a driver for the company)", example = "PROFILE",
                allowableValues = {"PROFILE", "DRIVER"})
        String subjectType
) {
}

package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserAuthResponse", description = "Authentication result for a web user: tokens plus account summary")
public record UserAuthResponse(

        @Schema(description = "Access and refresh tokens for the new session")
        TokenResponse tokens,

        @Schema(description = "Summary of the authenticated user")
        UserSummary user
) {
}

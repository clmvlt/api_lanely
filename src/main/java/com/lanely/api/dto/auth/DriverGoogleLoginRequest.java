package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "DriverGoogleLoginRequest", description = "Credentials to open a mobile delivery session for a web user who signed in with "
        + "Google, scoped to a company. The user must already exist and be a member of the company.")
public record DriverGoogleLoginRequest(

        @Schema(description = "Public code of the company (obtained by scanning the company code)", example = "K7P2M9QX", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String companyCode,

        @Schema(description = "Google ID token obtained on the client (Google Identity Services), verified server-side", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String idToken
) {
}

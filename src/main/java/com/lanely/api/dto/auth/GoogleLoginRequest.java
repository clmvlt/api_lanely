package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "GoogleLoginRequest", description = "Payload to log in (or sign up) a web user with a Google ID token")
public record GoogleLoginRequest(

        @Schema(
                description = "The Google ID token (a JWT signed by Google) obtained on the client via Google Identity Services / "
                        + "Sign in with Google. The backend verifies its signature, expiry and audience (the configured Google client id), "
                        + "then finds or creates the matching web user.",
                example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFhMmIzYzRkIn0.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIn0.signature",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String idToken
) {
}

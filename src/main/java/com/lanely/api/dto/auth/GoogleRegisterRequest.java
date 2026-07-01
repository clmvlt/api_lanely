package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "GoogleRegisterRequest",
        description = "Payload to confirm the creation of a new web user from a Google ID token, after the client showed the "
                + "pre-filled sign-up form returned by POST /auth/google (status = REGISTRATION_REQUIRED).")
public record GoogleRegisterRequest(

        @Schema(description = "The same Google ID token returned by the client during the initial /auth/google call. It is verified "
                + "again server-side; the email and Google identity are taken from the token (never from the client). Must still be "
                + "valid (Google ID tokens are short-lived, ~1h).",
                example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFhMmIzYzRkIn0.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIn0.signature",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String idToken,

        @Schema(description = "First name for the new account (pre-filled from Google, possibly edited by the user)",
                example = "Jane", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 100)
        String firstName,

        @Schema(description = "Last name for the new account (pre-filled from Google, possibly edited by the user)",
                example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 100)
        String lastName
) {
}

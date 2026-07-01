package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GoogleRegistrationDraft",
        description = "Pre-filled account data extracted from the verified Google ID token. Returned when no account exists yet, "
                + "so the client can display a confirmation form before creating the account. None of this is persisted until the "
                + "client confirms via POST /auth/google/register.")
public record GoogleRegistrationDraft(

        @Schema(description = "Google-verified email address that the new account will use (read-only on the form)",
                example = "jane.doe@gmail.com")
        String email,

        @Schema(description = "Suggested first name extracted from the Google profile (editable by the user)", example = "Jane")
        String firstName,

        @Schema(description = "Suggested last name extracted from the Google profile (editable by the user)", example = "Doe")
        String lastName
) {
}

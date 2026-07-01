package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GoogleAuthResponse",
        description = "Result of a Google sign-in attempt. Exactly one of the two outcomes applies, given by 'status':\n"
                + "- AUTHENTICATED: an account exists (or the Google identity was linked to an existing email) and a session was "
                + "issued; 'session' is populated and 'registration' is null.\n"
                + "- REGISTRATION_REQUIRED: no account exists yet; nothing was created. 'registration' holds the pre-filled data to "
                + "confirm on a sign-up form, and 'session' is null. Confirm via POST /auth/google/register with the same ID token.")
public record GoogleAuthResponse(

        @Schema(description = "Outcome discriminator", example = "REGISTRATION_REQUIRED")
        GoogleAuthStatus status,

        @Schema(description = "Issued session (tokens + user summary). Present only when status = AUTHENTICATED.", nullable = true)
        UserAuthResponse session,

        @Schema(description = "Pre-filled account creation data. Present only when status = REGISTRATION_REQUIRED.", nullable = true)
        GoogleRegistrationDraft registration
) {

    public static GoogleAuthResponse authenticated(UserAuthResponse session) {
        return new GoogleAuthResponse(GoogleAuthStatus.AUTHENTICATED, session, null);
    }

    public static GoogleAuthResponse registrationRequired(GoogleRegistrationDraft registration) {
        return new GoogleAuthResponse(GoogleAuthStatus.REGISTRATION_REQUIRED, null, registration);
    }
}

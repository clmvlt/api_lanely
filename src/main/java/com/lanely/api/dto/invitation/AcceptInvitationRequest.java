package com.lanely.api.dto.invitation;

import com.lanely.api.dto.auth.RegisterUserRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "AcceptInvitationRequest", description = "Payload to accept a company invitation. "
        + "If the caller is authenticated (Bearer token), the current user joins and 'newAccount' must be omitted. "
        + "If the caller is anonymous and has no account yet, 'newAccount' must be provided to create the account on the fly.")
public record AcceptInvitationRequest(

        @Schema(description = "Join code received in the invitation email/link", example = "Zm9vYmFyYmF6cXV4MTIzNDU2Nzg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String code,

        @Schema(description = "Account creation payload, only when accepting anonymously without an existing account", nullable = true)
        @Valid
        RegisterUserRequest newAccount
) {
}

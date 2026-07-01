package com.lanely.api.dto.invitation;

import com.lanely.api.dto.auth.UserAuthResponse;
import com.lanely.api.entity.enums.CompanyRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "AcceptInvitationResponse", description = "Result of accepting an invitation: the joined company plus, when a new account was created, fresh tokens.")
public record AcceptInvitationResponse(

        @Schema(description = "Identifier of the company that was joined", example = "11112222-3333-4444-5555-666677778888")
        UUID companyId,

        @Schema(description = "Name of the company that was joined", example = "Speedy Delivery")
        String companyName,

        @Schema(description = "Role granted to the user in the company", example = "MEMBER")
        CompanyRole role,

        @Schema(description = "Authentication tokens, present only when a new account was created during acceptance; null when an already-authenticated user accepted.", nullable = true)
        UserAuthResponse auth
) {
}

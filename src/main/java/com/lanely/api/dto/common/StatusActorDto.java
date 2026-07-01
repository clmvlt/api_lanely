package com.lanely.api.dto.common;

import com.lanely.api.entity.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "StatusActor", description = "The account that performed a status change")
public record StatusActorDto(

        @Schema(description = "Identifier of the acting account; null when the account has since been deleted",
                example = "11112222-3333-4444-5555-666677778888", nullable = true)
        UUID id,

        @Schema(description = "Type of the acting account: USER (web back-office) or PROFILE (mobile driver)",
                example = "PROFILE")
        AccountType type,

        @Schema(description = "Display name of the actor captured at the time of the change", example = "John Driver")
        String name
) {
}

package com.lanely.api.dto.me;

import com.lanely.api.entity.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MeResponse", description = "The currently authenticated account. Exactly one of 'user' or 'profile' is populated, depending on the token type.")
public record MeResponse(

        @Schema(description = "Type of the authenticated account", example = "USER")
        AccountType type,

        @Schema(description = "Populated when type is USER, null otherwise", nullable = true)
        UserMe user,

        @Schema(description = "Populated when type is PROFILE, null otherwise", nullable = true)
        ProfileMe profile
) {

    public static MeResponse ofUser(UserMe user) {
        return new MeResponse(AccountType.USER, user, null);
    }

    public static MeResponse ofProfile(ProfileMe profile) {
        return new MeResponse(AccountType.PROFILE, null, profile);
    }
}

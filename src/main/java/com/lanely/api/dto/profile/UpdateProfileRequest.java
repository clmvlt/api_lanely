package com.lanely.api.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateProfileRequest", description = "Payload to update a delivery profile. Only non-null fields are applied.")
public record UpdateProfileRequest(

        @Schema(description = "New username, unique within the company. Null to keep unchanged.", example = "driver02", nullable = true)
        @Size(min = 1, max = 100)
        String username,

        @Schema(description = "New display name. Null to keep unchanged.", example = "John (evening shift)", nullable = true)
        @Size(max = 150)
        String displayName,

        @Schema(description = "New password, at least 6 characters. Null to keep unchanged.", example = "newdrivpass", nullable = true, minLength = 6)
        @Size(min = 6, max = 100)
        String password
) {
}

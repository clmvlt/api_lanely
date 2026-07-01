package com.lanely.api.dto.me;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateMeRequest", description = "Update the current web user's own information. Only non-null fields are applied.")
public record UpdateMeRequest(

        @Schema(description = "New first name. Null to keep unchanged.", example = "Jane", nullable = true)
        @Size(min = 1, max = 100)
        String firstName,

        @Schema(description = "New last name. Null to keep unchanged.", example = "Doe", nullable = true)
        @Size(min = 1, max = 100)
        String lastName
) {
}

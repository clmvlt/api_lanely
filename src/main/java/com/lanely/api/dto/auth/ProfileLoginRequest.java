package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ProfileLoginRequest", description = "Credentials to open a mobile delivery session, scoped to a company. The identifier "
        + "is resolved in two ways: first as a delivery profile username within the company; if none matches, as the email of a web user "
        + "who is a member of the company (a web user acting as a driver). Web users signed up with Google have no usable password and must "
        + "use POST /auth/driver/google instead.")
public record ProfileLoginRequest(

        @Schema(description = "Public code of the company (obtained by scanning the company code)", example = "K7P2M9QX", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String companyCode,

        @Schema(description = "Identifier: either a delivery profile username (unique within the company) or the email of a web user "
                + "who is a member of the company", example = "driver01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String username,

        @Schema(description = "Password of the delivery profile or of the web user", example = "drivpass", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String password
) {
}

package com.lanely.api.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ChangeSubscriptionRequest", description = "Payload to switch the current user to another subscription plan")
public record ChangeSubscriptionRequest(

        @Schema(description = "Technical code of the target plan", example = "PRO", requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"STARTER", "PRO", "ENTERPRISE"})
        @NotBlank
        String planCode
) {
}

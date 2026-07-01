package com.lanely.api.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateClientContactRequest", description = "Payload to update a client contact. Only non-null fields are applied.")
public record UpdateClientContactRequest(

        @Schema(description = "New first name. Null to keep unchanged.", example = "Marie", nullable = true)
        @Size(max = 100)
        String firstName,

        @Schema(description = "New last name. Null to keep unchanged.", example = "Dupont", nullable = true)
        @Size(max = 100)
        String lastName,

        @Schema(description = "New job title. Null to keep unchanged.", example = "Operations director", nullable = true)
        @Size(max = 120)
        String jobTitle,

        @Schema(description = "New e-mail. Null to keep unchanged.", example = "marie.dupont@acme.example", nullable = true)
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "New phone. Null to keep unchanged.", example = "+33123456789", nullable = true)
        @Size(max = 32)
        String phone,

        @Schema(description = "Set/unset primary flag. Setting true unsets it on other contacts. Null to keep unchanged.", example = "true", nullable = true)
        Boolean isPrimary,

        @Schema(description = "Whether this contact receives invoices by e-mail. Null to keep unchanged.", example = "true", nullable = true)
        Boolean receivesInvoices,

        @Schema(description = "Whether this contact receives delivery notifications by e-mail. Null to keep unchanged.", example = "false", nullable = true)
        Boolean receivesDeliveryNotifications,

        @Schema(description = "Activate/deactivate the contact. Null to keep unchanged.", example = "true", nullable = true)
        Boolean active
) {
}

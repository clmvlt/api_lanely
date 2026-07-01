package com.lanely.api.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateClientContactRequest", description = "Payload to add a contact person to a client")
public record CreateClientContactRequest(

        @Schema(description = "First name", example = "Marie", nullable = true)
        @Size(max = 100)
        String firstName,

        @Schema(description = "Last name", example = "Dupont", nullable = true)
        @Size(max = 100)
        String lastName,

        @Schema(description = "Job title / role", example = "Logistics manager", nullable = true)
        @Size(max = 120)
        String jobTitle,

        @Schema(description = "E-mail address", example = "marie.dupont@acme.example", nullable = true)
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "Phone number", example = "+33123456789", nullable = true)
        @Size(max = 32)
        String phone,

        @Schema(description = "Mark as the client's primary contact (unsets the flag on other contacts)", example = "false", nullable = true)
        Boolean isPrimary,

        @Schema(description = "This contact receives invoices by e-mail", example = "true", nullable = true)
        Boolean receivesInvoices,

        @Schema(description = "This contact receives delivery notifications by e-mail", example = "false", nullable = true)
        Boolean receivesDeliveryNotifications
) {
}

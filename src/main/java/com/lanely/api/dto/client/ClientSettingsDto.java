package com.lanely.api.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "ClientSettingsDto", description = "Per-client preferences, mostly driving automatic e-mails. "
        + "On update, only non-null fields are applied.")
public record ClientSettingsDto(

        @Schema(description = "Language used for e-mails sent to this client (ISO 639-1). Supported: en, fr. Defaults to en.",
                example = "fr", defaultValue = "en", nullable = true)
        @Pattern(regexp = "^(en|fr)$", message = "{validation.client.language}")
        String preferredLanguage,

        @Schema(description = "Automatically e-mail invoices to this client when they are issued", example = "true", nullable = true)
        Boolean autoSendInvoiceEmail,

        @Schema(description = "Automatically e-mail delivery status notifications to this client", example = "true", nullable = true)
        Boolean autoSendDeliveryNotifications,

        @Schema(description = "Automatically e-mail payment reminders for overdue invoices", example = "false", nullable = true)
        Boolean autoSendPaymentReminders,

        @Schema(description = "Dedicated billing e-mail address overriding the client's main e-mail for invoicing", example = "billing@acme.example", nullable = true)
        @Email
        @Size(max = 255)
        String billingEmail
) {
}

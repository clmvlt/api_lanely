package com.lanely.api.email;

/**
 * A single outbound email. {@code htmlBody} is optional (null for a plain-text email).
 * Kept intentionally minimal; extend with cc/bcc/attachments/from-override when needed.
 */
public record EmailMessage(

        String to,

        String subject,

        String textBody,

        String htmlBody
) {

    public static EmailMessage text(String to, String subject, String textBody) {
        return new EmailMessage(to, subject, textBody, null);
    }

    public static EmailMessage html(String to, String subject, String textBody, String htmlBody) {
        return new EmailMessage(to, subject, textBody, htmlBody);
    }
}

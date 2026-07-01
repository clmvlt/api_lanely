package com.lanely.api.email;

/**
 * Transport abstraction for sending emails. The default implementation only logs the
 * message; provide a real implementation (SMTP via JavaMailSender, or an HTTP provider such
 * as SendGrid/Mailgun) and select it with {@code app.email.provider}.
 */
public interface EmailSender {

    void send(EmailMessage message);
}

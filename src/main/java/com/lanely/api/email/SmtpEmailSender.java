package com.lanely.api.email;

import com.lanely.api.config.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.email", name = "provider", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final EmailProperties properties;

    public SmtpEmailSender(JavaMailSender mailSender, EmailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void send(EmailMessage message) {
        try {
            if (message.htmlBody() != null) {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
                helper.setFrom(properties.from());
                helper.setTo(message.to());
                helper.setSubject(message.subject());
                helper.setText(message.textBody() == null ? "" : message.textBody(), message.htmlBody());
                mailSender.send(mime);
            } else {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(properties.from());
                mail.setTo(message.to());
                mail.setSubject(message.subject());
                mail.setText(message.textBody());
                mailSender.send(mail);
            }
            log.info("Email sent to '{}' (subject '{}')", message.to(), message.subject());
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to '{}' (subject '{}'): {}", message.to(), message.subject(), e.getMessage());
        }
    }
}

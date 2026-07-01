package com.lanely.api.service.mail;

import com.lanely.api.config.EmailProperties;
import com.lanely.api.email.EmailMessage;
import com.lanely.api.email.EmailSender;
import com.lanely.api.email.template.EmailLayout;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class DefaultMailService implements MailService {

    private final EmailSender emailSender;
    private final MessageSource messageSource;
    private final EmailProperties emailProperties;

    public DefaultMailService(EmailSender emailSender, MessageSource messageSource,
                              EmailProperties emailProperties) {
        this.emailSender = emailSender;
        this.messageSource = messageSource;
        this.emailProperties = emailProperties;
    }

    @Override
    public void sendInvitation(String toEmail, String companyName, String code, String joinLink) {
        Locale locale = LocaleContextHolder.getLocale();
        String subject = msg("mail.invitation.subject", locale, companyName);
        String html = layout(locale)
                .preheader(msg("mail.invitation.preheader", locale, companyName))
                .title(msg("mail.invitation.title", locale, companyName))
                .addParagraph(msg("mail.layout.greeting", locale))
                .addParagraph(msg("mail.invitation.intro", locale, companyName))
                .cta(msg("mail.invitation.cta", locale), joinLink)
                .note(msg("mail.invitation.note", locale))
                .render();
        String text = msg("mail.invitation.body", locale, companyName, joinLink);
        emailSender.send(EmailMessage.html(toEmail, subject, text, html));
    }

    @Override
    public void sendEmailVerification(String toEmail, String verifyLink) {
        Locale locale = LocaleContextHolder.getLocale();
        String subject = msg("mail.verification.subject", locale);
        String html = layout(locale)
                .preheader(msg("mail.verification.preheader", locale))
                .title(msg("mail.verification.title", locale))
                .addParagraph(msg("mail.layout.greeting", locale))
                .addParagraph(msg("mail.verification.intro", locale))
                .cta(msg("mail.verification.cta", locale), verifyLink)
                .note(msg("mail.verification.note", locale))
                .render();
        String text = msg("mail.verification.body", locale, verifyLink);
        emailSender.send(EmailMessage.html(toEmail, subject, text, html));
    }

    @Override
    public void sendPasswordReset(String toEmail, String resetLink) {
        Locale locale = LocaleContextHolder.getLocale();
        String subject = msg("mail.password-reset.subject", locale);
        String html = layout(locale)
                .preheader(msg("mail.password-reset.preheader", locale))
                .title(msg("mail.password-reset.title", locale))
                .addParagraph(msg("mail.layout.greeting", locale))
                .addParagraph(msg("mail.password-reset.intro", locale))
                .cta(msg("mail.password-reset.cta", locale), resetLink)
                .note(msg("mail.password-reset.note", locale))
                .render();
        String text = msg("mail.password-reset.body", locale, resetLink);
        emailSender.send(EmailMessage.html(toEmail, subject, text, html));
    }

    private EmailLayout layout(Locale locale) {
        return EmailLayout.builder()
                .lang(locale.getLanguage())
                .footer(msg("mail.layout.footer", locale))
                .support(msg("mail.layout.support", locale), emailProperties.supportUrl());
    }

    private String msg(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args.length == 0 ? null : args, locale);
    }
}

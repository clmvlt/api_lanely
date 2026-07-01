package com.lanely.api.service.mail;

public interface MailService {

    void sendInvitation(String toEmail, String companyName, String code, String joinLink);

    void sendEmailVerification(String toEmail, String verifyLink);

    void sendPasswordReset(String toEmail, String resetLink);
}

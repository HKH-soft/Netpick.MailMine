package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.email.dto.EmailRequest;

import java.util.Map;

public interface EmailService {

    /**
     * Send a simple text email
     */
    void sendSimpleMail(EmailRequest request);

    /**
     * Send an email with attachment
     */
    void sendMailWithAttachment(EmailRequest request);

    /**
     * Send an HTML email using a Thymeleaf template
     */
    void sendTemplatedEmail(String recipient, String subject, String templateName, Map<String, Object> variables);

    /**
     * Send verification email to a user (for auth)
     */
    void sendVerificationEmail(String email, String code, int expirationMinutes);

    /**
     * Send mass emails to multiple recipients
     */
    void sendMassEmail(EmailRequest request);
}

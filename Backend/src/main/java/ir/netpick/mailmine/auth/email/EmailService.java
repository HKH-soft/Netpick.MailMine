package ir.netpick.mailmine.auth.email;

public interface EmailService {
    void sendSimpleMail(EmailDetails details);
    void sendMailWithAttachment(EmailDetails details);
    void sendVerificationEmailWithTemplate(String recipient, String code);
    void sendVerificationEmail(String email);
}
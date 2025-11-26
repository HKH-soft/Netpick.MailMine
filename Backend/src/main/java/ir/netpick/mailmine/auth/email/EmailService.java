package ir.netpick.mailmine.auth.email;

public interface EmailService {
    String sendSimpleMail(EmailDetails details);
    String sendMailWithAttachment(EmailDetails details);
    String sendVerificationEmailWithTemplate(String recipient, String code);
    String sendVerificationEmail(String email);
}
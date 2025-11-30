package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.email.dto.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.time.Year;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Log4j2
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public void sendSimpleMail(EmailRequest request) {
        try {
            log.info("Sending simple mail to: {}", request.getRecipient());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(request.getRecipient());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());

            javaMailSender.send(message);
            log.info("Simple mail sent successfully to: {}", request.getRecipient());
        } catch (Exception e) {
            log.error("Error while sending simple mail to: {}", request.getRecipient(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    @Async
    public void sendMailWithAttachment(EmailRequest request) {
        log.info("Sending email with attachment to: {}", request.getRecipient());
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(sender);
            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody());

            if (request.getAttachment() != null && !request.getAttachment().isEmpty()) {
                FileSystemResource file = new FileSystemResource(new File(request.getAttachment()));
                helper.addAttachment(file.getFilename(), file);
            }

            javaMailSender.send(mimeMessage);
            log.info("Email with attachment sent successfully to: {}", request.getRecipient());
        } catch (MessagingException e) {
            log.error("Error while sending email with attachment to: {}", request.getRecipient(), e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    @Override
    @Async
    public void sendTemplatedEmail(String recipient, String subject, String templateName,
            Map<String, Object> variables) {
        log.info("Sending templated email to: {}", recipient);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(sender);
            helper.setTo(recipient);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariables(variables);
            context.setVariable("currentYear", Year.now().getValue());

            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            log.info("Templated email sent successfully to: {}", recipient);
        } catch (MessagingException e) {
            log.error("Error while sending templated email to: {}", recipient, e);
            throw new RuntimeException("Failed to send templated email", e);
        }
    }

    @Override
    @Async
    public void sendVerificationEmail(String email, String code, int expirationMinutes) {
        log.info("Sending verification email to: {}", email);

        Map<String, Object> variables = Map.of(
                "code", code,
                "expirationTime", expirationMinutes);

        sendTemplatedEmail(email, "Email Verification Code", "email/verification-email", variables);
    }

    @Override
    @Async
    public void sendMassEmail(EmailRequest request) {
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            log.warn("No recipients provided for mass email");
            return;
        }

        log.info("Sending mass email to {} recipients", request.getRecipients().size());

        int successCount = 0;
        int failCount = 0;

        for (String recipient : request.getRecipients()) {
            try {
                EmailRequest singleRequest = EmailRequest.builder()
                        .recipient(recipient)
                        .subject(request.getSubject())
                        .body(request.getBody())
                        .build();

                sendSimpleMail(singleRequest);
                successCount++;

                // Small delay to avoid rate limiting
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("Failed to send email to: {}", recipient, e);
                failCount++;
            }
        }

        log.info("Mass email completed. Success: {}, Failed: {}", successCount, failCount);
    }
}

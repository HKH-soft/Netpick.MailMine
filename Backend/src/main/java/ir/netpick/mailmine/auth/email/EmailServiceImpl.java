package ir.netpick.mailmine.auth.email;

import ir.netpick.mailmine.auth.AuthConstants;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.xml.bind.ValidationException;
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

@RequiredArgsConstructor
@Service
@Log4j2
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendSimpleMail(EmailDetails details)
    {
        try {
            log.info("Sending simple mail to: {}", details.getRecipient());
            
            // Creating a simple mail message
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

            // Setting up necessary details
            simpleMailMessage.setFrom(sender);
            simpleMailMessage.setTo(details.getRecipient());
            simpleMailMessage.setText(details.getMsgBody());
            simpleMailMessage.setSubject(details.getSubject());

            // Sending the mail
            javaMailSender.send(simpleMailMessage);
            log.info("Simple mail sent successfully to: {}", details.getRecipient());
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            log.error("Error while sending simple mail to: {}", details.getRecipient(), e);
        }
    }
    
    @Override
    @Async
    public void sendVerificationEmail(String email) {
        try {
            log.info("Sending verification email to: {}", email);
            
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(sender);
            helper.setTo(email);
            helper.setSubject("Email Verification Code");

            User user = userService.getUserEntity(email);
            if(user.getVerification().isExpired()){
                log.warn("Verification code expired for user: {}", email);
                throw new ValidationException("The verification code for your account has expired. A new code will be generated and sent.");
            }
            String code = user.getVerification().getCode();

            // Prepare the template context
            Context context = new Context();
            context.setVariable("code", code);
            context.setVariable("expirationTime", AuthConstants.VERIFICATION_CODE_EXPIRATION_TIME_MIN);
            context.setVariable("currentYear", java.time.Year.now().getValue());

            // Process the template
            String htmlContent = templateEngine.process("verification-email", context);

            helper.setText(htmlContent, true);

            // Sending the mail
            javaMailSender.send(mimeMessage);
            log.info("Verification email sent successfully to: {}", email);
        }
        catch (Exception e) {
            log.error("Error sending verification email to: {}", email, e);
            // Note: Since this is async, we can't throw exceptions back to the caller
            // In a production environment, you might want to store failed emails for retry
        }
    }
    
    @Async
    public void sendVerificationEmailWithTemplate(String recipient, String code) {
        // Creating a mime message
        log.info("Sending verification email with template to: {}", recipient);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            // Setting multipart as true for attachments
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
  
            // Adding the attachment
            //FileSystemResource file = new FileSystemResource(new File(details.getAttachment()));
  
            // Add the recipient, subject, and body to the email
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(recipient);
            mimeMessageHelper.setText("Verification code: " + code);
            mimeMessageHelper.setSubject("Email Verification");
  
            // Add the file attachment to the email
            //mimeMessageHelper.addAttachment(file.getFilename(), file);
  
            // Sending the mail
            javaMailSender.send(mimeMessage);
            log.info("Verification email with template sent successfully to: {}", recipient);
        }
        catch (MessagingException e) {
            // Display message when mail sent failed
            log.error("Error while sending verification email with template to: {}", recipient, e);
        }
    }

    // Method 2
    // To send an email with attachment
    @Async
    public void sendMailWithAttachment(EmailDetails details)
    {
        log.info("Sending email with attachment to: {}", details.getRecipient());
        // Creating a mime message
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            // Setting multipart as true for attachments
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
  
            // Adding the attachment
            FileSystemResource file = new FileSystemResource(new File(details.getAttachment()));
  
            // Add the recipient, subject, and body to the email
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMsgBody());
            mimeMessageHelper.setSubject(details.getSubject());
  
            // Add the file attachment to the email
            mimeMessageHelper.addAttachment(file.getFilename(), file);
  
            // Sending the mail
            javaMailSender.send(mimeMessage);
            log.info("Email with attachment sent successfully to: {}", details.getRecipient());
        }
        catch (MessagingException e) {
            // Display message when mail sent failed
            log.error("Error while sending email with attachment to: {}", details.getRecipient(), e);
        }
    }
}
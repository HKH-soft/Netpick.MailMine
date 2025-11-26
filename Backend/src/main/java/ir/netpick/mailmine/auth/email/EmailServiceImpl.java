package ir.netpick.mailmine.auth.email;

import ir.netpick.mailmine.auth.AuthConstants;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.xml.bind.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;

@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String sender;

    public String sendSimpleMail(EmailDetails details)
    {
        try {
            // Creating a simple mail message
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

            // Setting up necessary details
            simpleMailMessage.setFrom(sender);
            simpleMailMessage.setTo(details.getRecipient());
            simpleMailMessage.setText(details.getMsgBody());
            simpleMailMessage.setSubject(details.getSubject());

            // Sending the mail
            javaMailSender.send(simpleMailMessage);
            return "Mail Sent Successfully...";
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            return "Error while Sending Mail";
        }
    }
    
    @Override
    public String sendVerificationEmail(String email) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(sender);
            helper.setTo(email);
            helper.setSubject("Email Verification Code");

            User user = userService.getUserEntity(email);
            if(user.getVerification().isExpired()){
                throw new ValidationException("verification code is expired");
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
            return "Mail sent Successfully";


        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String sendVerificationEmailWithTemplate(String recipient, String code) {
        // Creating a mime message
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
            return "Mail sent Successfully";
        }
        catch (MessagingException e) {
            // Display message when mail sent failed
            return "Error while sending mail!!!";
        }
    }

    // Method 2
    // To send an email with attachment
    public String sendMailWithAttachment(EmailDetails details)
    {
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
            return "Mail sent Successfully";
        }
        catch (MessagingException e) {
            // Display message when mail sent failed
            return "Error while sending mail!!!";
        }
    }
}
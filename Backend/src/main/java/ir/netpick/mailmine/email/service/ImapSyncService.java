package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Part;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImapSyncService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailClassificationService emailClassificationService;

    @Value("${mail.imap.host:}")
    private String imapHost;

    @Value("${mail.imap.port:993}")
    private String imapPort;

    @Value("${mail.imap.username:}")
    private String imapUsername;

    @Value("${mail.imap.password:}")
    private String imapPassword;

    @Value("${mail.imap.enabled:false}")
    private boolean imapEnabled;

    /**
     * Sync emails from IMAP server - runs every 5 minutes
     */
    @Scheduled(fixedDelay = 300000)
    public void syncEmails() {
        if (!imapEnabled) {
            log.debug("IMAP sync disabled");
            return;
        }

        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imaps.host", imapHost);
            props.setProperty("mail.imaps.port", imapPort);
            props.setProperty("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect(imapUsername, imapPassword);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            int processed = 0;

            for (Message message : messages) {
                if (isNewMessage(message)) {
                    EmailMessage emailMessage = mapToEmailMessage(message);
                    emailMessageRepository.save(emailMessage);
                    emailClassificationService.classifyEmail(emailMessage);
                    processed++;
                }
            }

            log.info("Processed {} new emails from IMAP", processed);
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            log.error("Failed to sync emails from IMAP: {}", e.getMessage(), e);
        }
    }

    private boolean isNewMessage(Message message) throws MessagingException {
        String messageId = message.getHeader("Message-ID")[0];
        return emailMessageRepository.findByMessageId(messageId).isEmpty();
    }

    private EmailMessage mapToEmailMessage(Message message) throws Exception {
        EmailMessage email = new EmailMessage();
        email.setMessageId(message.getHeader("Message-ID")[0]);
        email.setThreadId(getHeaderValue(message, "References"));
        email.setSubject(message.getSubject());
        email.setReceivedAt(LocalDateTime.ofInstant(
                message.getReceivedDate().toInstant(), 
                java.time.ZoneId.systemDefault()));

        // Sender
        Address[] from = message.getFrom();
        if (from != null && from.length > 0) {
            InternetAddress sender = (InternetAddress) from[0];
            email.setSenderEmail(sender.getAddress());
            email.setSenderName(sender.getPersonal());
        }

        // Recipients
        for (Address addr : message.getAllRecipients()) {
            email.getRecipients().add(((InternetAddress) addr).getAddress());
        }

        // Body
        if (message.isMimeType("text/plain")) {
            email.setBodyText(message.getContent().toString());
        } else if (message.isMimeType("text/html")) {
            email.setBodyHtml(message.getContent().toString());
        } else if (message.isMimeType("multipart/*")) {
            extractFromMultipart(message, email);
        }

        // Attachments
        email.setHasAttachments(hasAttachments(message));

        return email;
    }

    private boolean hasAttachments(Message message) throws Exception {
        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void extractFromMultipart(Message message, EmailMessage email) throws Exception {
        Multipart multipart = (Multipart) message.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain") && email.getBodyText() == null) {
                email.setBodyText(bodyPart.getContent().toString());
            } else if (bodyPart.isMimeType("text/html") && email.getBodyHtml() == null) {
                email.setBodyHtml(bodyPart.getContent().toString());
            }
        }
    }

    private String getHeaderValue(Message message, String header) throws MessagingException {
        String[] values = message.getHeader(header);
        return values != null && values.length > 0 ? values[0] : null;
    }
}
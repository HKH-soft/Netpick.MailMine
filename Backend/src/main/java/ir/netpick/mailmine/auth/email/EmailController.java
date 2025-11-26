package ir.netpick.mailmine.auth.email;

import ir.netpick.mailmine.auth.model.Verification;
import ir.netpick.mailmine.auth.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
// Class
public class EmailController {

    private final EmailService emailService;
    private final VerificationService verificationService;

    @PostMapping("/sendMail")
    public ResponseEntity<String> sendMail(@RequestBody EmailDetails details)
    {
        return ResponseEntity.ok()
                .body(emailService.sendSimpleMail(details));
    }

    @PostMapping("/sendMailWithCode")
    public ResponseEntity<String> sendMailWithCode(@RequestBody EmailDetails details)
    {
        return ResponseEntity.ok()
                .body(emailService.sendVerificationEmailWithTemplate(details.getRecipient(),verificationService.generateVerificationCode()));
    }

    // Sending email with attachment
    @PostMapping("/sendMailWithAttachment")
    public ResponseEntity<String> sendMailWithAttachment(
            @RequestBody EmailDetails details)
    {

        return ResponseEntity.ok()
                        .body(emailService.sendMailWithAttachment(details));
    }
}
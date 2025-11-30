package ir.netpick.mailmine.email.controller;

import ir.netpick.mailmine.email.dto.EmailRequest;
import ir.netpick.mailmine.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class EmailController {

    private final EmailService emailService;

    /**
     * Send a simple email
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest request) {
        emailService.sendSimpleMail(request);
        return ResponseEntity.ok(Map.of("message", "Email sent successfully"));
    }

    /**
     * Send an email with attachment
     */
    @PostMapping("/send-with-attachment")
    public ResponseEntity<Map<String, String>> sendEmailWithAttachment(@RequestBody EmailRequest request) {
        emailService.sendMailWithAttachment(request);
        return ResponseEntity.ok(Map.of("message", "Email with attachment sent successfully"));
    }

    /**
     * Send mass emails to multiple recipients
     */
    @PostMapping("/send-mass")
    public ResponseEntity<Map<String, String>> sendMassEmail(@RequestBody EmailRequest request) {
        emailService.sendMassEmail(request);
        return ResponseEntity.ok(Map.of(
                "message", "Mass email job started",
                "recipientCount", String.valueOf(request.getRecipients().size())));
    }
}

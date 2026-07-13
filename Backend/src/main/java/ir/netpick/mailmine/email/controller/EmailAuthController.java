package ir.netpick.mailmine.email.controller;

import ir.netpick.mailmine.email.service.EmailAuthValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/email-auth")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class EmailAuthController {

    private final EmailAuthValidationService validationService;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateDomain(@RequestParam String domain) {
        return ResponseEntity.ok(validationService.validateDomain(domain));
    }

    @GetMapping("/validate-email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam String email) {
        return ResponseEntity.ok(validationService.validateFromEmail(email));
    }
}

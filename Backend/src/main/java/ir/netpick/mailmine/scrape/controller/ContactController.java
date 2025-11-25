package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.service.base.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/scrape/contacts")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ContactController {
    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<?> allContacts(@RequestParam(defaultValue = "1") int page){
        return ResponseEntity.ok()
                .body(contactService.allContacts(page));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getContact(@PathVariable UUID id){
        return ResponseEntity.ok()
                .body(contactService.getContact(id));
    }
}

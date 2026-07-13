package ir.netpick.mailmine.email.controller;

import ir.netpick.mailmine.email.model.EmailTemplate;
import ir.netpick.mailmine.email.repository.EmailTemplateRepository;
import ir.netpick.mailmine.email.service.TemplateRenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class EmailTemplateController {

    private final EmailTemplateRepository emailTemplateRepository;
    private final TemplateRenderService templateRenderService;

    @GetMapping
    public ResponseEntity<List<EmailTemplate>> listTemplates() {
        return ResponseEntity.ok(emailTemplateRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplate> getTemplate(@PathVariable UUID id) {
        return emailTemplateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EmailTemplate> createTemplate(@RequestBody EmailTemplate template) {
        return ResponseEntity.ok(emailTemplateRepository.save(template));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplate> updateTemplate(
            @PathVariable UUID id, 
            @RequestBody EmailTemplate template) {
        return emailTemplateRepository.findById(id)
                .map(existing -> {
                    existing.setName(template.getName());
                    existing.setDescription(template.getDescription());
                    existing.setCategory(template.getCategory());
                    existing.setSubjectTemplate(template.getSubjectTemplate());
                    existing.setBodyTemplate(template.getBodyTemplate());
                    return ResponseEntity.ok(emailTemplateRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        return emailTemplateRepository.findById(id)
                .map(template -> {
                    emailTemplateRepository.delete(template);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/render")
    public ResponseEntity<TemplateRenderService.RenderedTemplate> renderTemplate(
            @PathVariable UUID id,
            @RequestBody Map<String, String> variables) {
        try {
            return ResponseEntity.ok(templateRenderService.renderById(id, variables));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
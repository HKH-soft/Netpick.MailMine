package ir.netpick.platform.mailmine.controller;

import ir.netpick.platform.mailmine.model.EmailRule;
import ir.netpick.platform.mailmine.repository.EmailRuleRepository;
import ir.netpick.platform.mailmine.service.RulesEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mailmine/email-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class RulesController {

    private final EmailRuleRepository emailRuleRepository;
    private final RulesEngineService rulesEngineService;

    @GetMapping
    public ResponseEntity<List<EmailRule>> listRules() {
        return ResponseEntity.ok(emailRuleRepository.findByIsActiveTrueOrderByPriorityDesc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailRule> getRule(@PathVariable UUID id) {
        return emailRuleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EmailRule> createRule(@RequestBody EmailRule rule) {
        return ResponseEntity.ok(emailRuleRepository.save(rule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailRule> updateRule(
            @PathVariable UUID id,
            @RequestBody EmailRule rule) {
        return emailRuleRepository.findById(id)
                .map(existing -> {
                    existing.setName(rule.getName());
                    existing.setDescription(rule.getDescription());
                    existing.setConditionType(rule.getConditionType());
                    existing.setConditionValue(rule.getConditionValue());
                    existing.setActionType(rule.getActionType());
                    existing.setActionValue(rule.getActionValue());
                    existing.setPriority(rule.getPriority());
                    existing.setIsActive(rule.getIsActive());
                    return ResponseEntity.ok(emailRuleRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        emailRuleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<?> testRule(
            @PathVariable UUID id,
            @RequestBody ir.netpick.platform.mailmine.model.EmailMessage testEmail) {
        rulesEngineService.evaluateRules(testEmail);
        return ResponseEntity.ok().build();
    }
}










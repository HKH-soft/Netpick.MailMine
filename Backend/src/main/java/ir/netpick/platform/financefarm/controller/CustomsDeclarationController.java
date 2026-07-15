package ir.netpick.platform.financefarm.controller;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.result.Result;
import ir.netpick.platform.financefarm.model.CustomsDeclaration;
import ir.netpick.platform.financefarm.service.CustomsDeclarationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customs-declarations")
@RequiredArgsConstructor
public class CustomsDeclarationController {

    private final CustomsDeclarationService customsDeclarationService;

    @GetMapping
    public ResponseEntity<PageDTO<CustomsDeclaration>> getAll(
            @RequestParam(defaultValue = "1") int pageNumber) {
        return ResponseEntity.ok(customsDeclarationService.getAll(pageNumber));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<PageDTO<CustomsDeclaration>> getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int pageNumber) {
        return ResponseEntity.ok(customsDeclarationService.getByStatus(status, pageNumber));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomsDeclaration> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(customsDeclarationService.getById(id));
    }

    @GetMapping("/number/{declarationNumber}")
    public ResponseEntity<CustomsDeclaration> getByDeclarationNumber(@PathVariable String declarationNumber) {
        return ResponseEntity.ok(customsDeclarationService.getByDeclarationNumber(declarationNumber));
    }

    @PostMapping
    public ResponseEntity<CustomsDeclaration> create(@RequestBody CustomsDeclaration declaration) {
        return ResponseEntity.ok(customsDeclarationService.create(declaration));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomsDeclaration> update(@PathVariable UUID id, @RequestBody CustomsDeclaration declaration) {
        return ResponseEntity.ok(customsDeclarationService.update(id, declaration));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customsDeclarationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<CustomsDeclaration> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(customsDeclarationService.submitForApproval(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<CustomsDeclaration> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(customsDeclarationService.approve(id));
    }
}
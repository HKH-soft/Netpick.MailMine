package ir.netpick.platform.financefarm.controller;

import ir.netpick.platform.core.result.Result;
import ir.netpick.platform.core.result.ResultGenerator;
import ir.netpick.platform.financefarm.model.CustomsDeclaration;
import ir.netpick.platform.financefarm.service.CustomsDeclarationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customs-declarations")
@RequiredArgsConstructor
public class CustomsDeclarationController {

    private final CustomsDeclarationService customsDeclarationService;

    @GetMapping
    public Result getAll(
            @RequestParam(defaultValue = "1") int pageNumber) {
        return ResultGenerator.success(customsDeclarationService.getAll(pageNumber));
    }

    @GetMapping("/status/{status}")
    public Result getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int pageNumber) {
        return ResultGenerator.success(customsDeclarationService.getByStatus(status, pageNumber));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable UUID id) {
        return ResultGenerator.success(customsDeclarationService.getById(id));
    }

    @GetMapping("/number/{declarationNumber}")
    public Result getByDeclarationNumber(@PathVariable String declarationNumber) {
        return ResultGenerator.success(customsDeclarationService.getByDeclarationNumber(declarationNumber));
    }

    @PostMapping
    public Result create(@RequestBody CustomsDeclaration declaration) {
        return ResultGenerator.success(customsDeclarationService.create(declaration));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable UUID id, @RequestBody CustomsDeclaration declaration) {
        return ResultGenerator.success(customsDeclarationService.update(id, declaration));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable UUID id) {
        customsDeclarationService.delete(id);
        return ResultGenerator.success("Customs declaration deleted");
    }

    @PostMapping("/{id}/submit")
    public Result submit(@PathVariable UUID id) {
        return ResultGenerator.success(customsDeclarationService.submitForApproval(id));
    }

    @PostMapping("/{id}/approve")
    public Result approve(@PathVariable UUID id) {
        return ResultGenerator.success(customsDeclarationService.approve(id));
    }
}
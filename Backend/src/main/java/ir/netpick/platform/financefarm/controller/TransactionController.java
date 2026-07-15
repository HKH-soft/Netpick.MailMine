package ir.netpick.platform.financefarm.controller;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.financefarm.model.Transaction;
import ir.netpick.platform.financefarm.service.TransactionService;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * FinanceFarm - Transaction Controller
 */
@RestController
@RequestMapping("/api/v1/financefarm/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageDTO<Transaction>> getAllTransactions(
            @RequestParam(defaultValue = "1") int page,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.getAll(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Transaction> createTransaction(
            @RequestBody Transaction transaction,
            @AuthenticationPrincipal User user) {
        transaction.setCreatedBy(user.getId());
        return ResponseEntity.ok(transactionService.create(transaction));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable UUID id,
            @RequestBody Transaction transaction,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.update(id, transaction));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> importTransactions(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {
        transactionService.importFromCsv(file, user.getId());
        return ResponseEntity.ok("Imported successfully");
    }
}
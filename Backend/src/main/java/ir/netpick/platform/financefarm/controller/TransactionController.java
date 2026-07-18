package ir.netpick.platform.financefarm.controller;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.enums.RoleEnum;
import ir.netpick.platform.financefarm.model.Transaction;
import ir.netpick.platform.financefarm.service.TransactionService;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
        // Filter by authenticated user's ownership
        return ResponseEntity.ok(transactionService.getByCreatedBy(user.getId(), page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        Transaction transaction = transactionService.getById(id);
        // Verify ownership
        if (!transaction.getCreatedBy().equals(user.getId()) && user.getRole().getName() != RoleEnum.SUPER_ADMIN) {
            throw new AccessDeniedException("Access denied to this transaction");
        }
        return ResponseEntity.ok(transaction);
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
        Transaction existing = transactionService.getById(id);
        // Verify ownership
        if (!existing.getCreatedBy().equals(user.getId()) && user.getRole().getName() != RoleEnum.SUPER_ADMIN) {
            throw new AccessDeniedException("Access denied to this transaction");
        }
        return ResponseEntity.ok(transactionService.update(id, transaction));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        Transaction transaction = transactionService.getById(id);
        // Verify ownership
        if (!transaction.getCreatedBy().equals(user.getId()) && user.getRole().getName() != RoleEnum.SUPER_ADMIN) {
            throw new AccessDeniedException("Access denied to this transaction");
        }
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
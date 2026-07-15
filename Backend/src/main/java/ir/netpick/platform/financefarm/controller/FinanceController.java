package ir.netpick.platform.financefarm.controller;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.financefarm.dto.FinanceSummaryDTO;
import ir.netpick.platform.financefarm.model.Invoice;
import ir.netpick.platform.financefarm.model.InvoiceStatus;
import ir.netpick.platform.financefarm.service.InvoiceService;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * FinanceFarm - Accounting and Invoicing Controller
 */
@RestController
@RequestMapping("/api/v1/financefarm/invoices")
@RequiredArgsConstructor
public class FinanceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageDTO<Invoice>> getAllInvoices(
            @RequestParam(defaultValue = "1") int page,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invoiceService.getAll(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Invoice> getInvoice(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice, @AuthenticationPrincipal User user) {
        invoice.setCreatedBy(user.getId());
        return ResponseEntity.ok(invoiceService.create(invoice));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Invoice> updateInvoice(
            @PathVariable UUID id,
            @RequestBody Invoice invoice,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invoiceService.update(id, invoice));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Invoice> updateInvoiceStatus(
            @PathVariable UUID id,
            @RequestParam InvoiceStatus status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invoiceService.updateStatus(id, status));
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceSummaryDTO> getFinanceSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invoiceService.getFinanceSummary());
    }
}
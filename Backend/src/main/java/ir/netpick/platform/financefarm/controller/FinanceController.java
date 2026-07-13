package ir.netpick.platform.financefarm.controller;

import ir.netpick.platform.financefarm.dto.InvoiceDTO;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * FinanceFarm - Accounting and Invoicing Controller
 */
@RestController
@RequestMapping("/api/v1/financefarm/invoices")
@RequiredArgsConstructor
public class FinanceController {

    // TODO: Add FinanceService dependency

    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices(@AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoice(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(null);
    }

    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(@RequestBody InvoiceDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDTO> updateInvoice(@PathVariable UUID id, @RequestBody InvoiceDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
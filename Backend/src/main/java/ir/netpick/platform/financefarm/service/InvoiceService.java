package ir.netpick.platform.financefarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.financefarm.dto.FinanceSummaryDTO;
import ir.netpick.platform.financefarm.dto.InvoiceDTO;
import ir.netpick.platform.financefarm.dto.InvoiceLineItemDTO;
import ir.netpick.platform.financefarm.model.Invoice;
import ir.netpick.platform.financefarm.model.InvoiceLineItem;
import ir.netpick.platform.financefarm.model.InvoiceStatus;
import ir.netpick.platform.financefarm.repository.InvoiceRepository;
import ir.netpick.platform.financefarm.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final TransactionRepository transactionRepository;

    public PageDTO<Invoice> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Invoice> page = invoiceRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Invoice> getByStatus(InvoiceStatus status, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Invoice> page = invoiceRepository.findByStatusAndDeletedFalse(status, pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Invoice> getByCreatedBy(UUID createdBy, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Invoice> page = invoiceRepository.findByCreatedByIdAndDeletedFalse(createdBy, pageable);
        return PageDTOMapper.map(page);
    }

    public Invoice getById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice with id [%s] was not found".formatted(invoiceId)));
    }

    public Invoice create(Invoice invoice) {
        invoice.setId(null);
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
            invoice.setInvoiceNumber(generateInvoiceNumber());
        }
        return invoiceRepository.save(invoice);
    }

    public Invoice update(UUID invoiceId, Invoice invoice) {
        Invoice existing = getById(invoiceId);
        invoice.setId(invoiceId);
        invoice.setCreatedAt(existing.getCreatedAt());
        return invoiceRepository.save(invoice);
    }

    public void delete(UUID invoiceId) {
        invoiceRepository.softDelete(invoiceId);
    }

    public void restore(UUID invoiceId) {
        invoiceRepository.restore(invoiceId);
    }

    public Invoice updateStatus(UUID invoiceId, InvoiceStatus status) {
        Invoice invoice = getById(invoiceId);
        invoice.setStatus(status);
        if (status == InvoiceStatus.PAID && invoice.getPaidAt() == null) {
            invoice.setPaidAt(LocalDateTime.now());
        }
        return invoiceRepository.save(invoice);
    }

    public void addLineItem(UUID invoiceId, InvoiceLineItem lineItem) {
        lineItem.setInvoiceId(invoiceId);
        lineItem.setId(null);
        // Calculate total
        if (lineItem.getQuantity() != null && lineItem.getUnitPrice() != null) {
            lineItem.setTotal(lineItem.getUnitPrice().multiply(BigDecimal.valueOf(lineItem.getQuantity())));
        }
        // Note: Would need InvoiceLineItemRepository to save
    }

    public FinanceSummaryDTO getFinanceSummary() {
        long totalInvoices = invoiceRepository.countByDeletedFalse();
        long paidInvoices = invoiceRepository.countByStatusAndDeletedFalse(InvoiceStatus.PAID);
        long overdueInvoices = invoiceRepository.countByStatusAndDeletedFalse(InvoiceStatus.OVERDUE);

        BigDecimal totalRevenue = invoiceRepository.sumTotalAmountByStatus(InvoiceStatus.PAID);
        BigDecimal totalExpenses = BigDecimal.ZERO; // Would need to query transactions

        return new FinanceSummaryDTO(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                totalExpenses,
                totalRevenue != null ? totalRevenue.subtract(totalExpenses) : BigDecimal.ZERO,
                Map.of(), // Revenue by month - would need aggregation
                Map.of(), // Expenses by month - would need aggregation
                totalInvoices,
                paidInvoices,
                overdueInvoices
        );
    }

    private String generateInvoiceNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return "INV-" + timestamp;
    }
}
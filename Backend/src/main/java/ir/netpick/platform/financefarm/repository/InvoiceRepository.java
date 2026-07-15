package ir.netpick.platform.financefarm.repository;

import ir.netpick.platform.financefarm.model.Invoice;
import ir.netpick.platform.financefarm.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // Find all non-deleted invoices with pagination
    Page<Invoice> findByDeletedFalse(Pageable pageable);

    // Count all non-deleted invoices
    long countByDeletedFalse();

    // Find invoices by status with pagination
    Page<Invoice> findByStatusAndDeletedFalse(InvoiceStatus status, Pageable pageable);

    // Find invoices by creator with pagination
    Page<Invoice> findByCreatedByIdAndDeletedFalse(UUID createdBy, Pageable pageable);

    // Count invoices by status
    @Query("select count(i) from Invoice i where i.deleted = false and i.status = ?1")
    long countByStatusAndDeletedFalse(InvoiceStatus status);

    // Sum total amount by status
    @Query("select sum(i.totalAmount) from Invoice i where i.deleted = false and i.status = ?1")
    BigDecimal sumTotalAmountByStatus(InvoiceStatus status);

    // Soft delete
    @Transactional
    @Modifying
    @Query("update Invoice i set i.deleted = true where i.deleted = false and i.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update Invoice i set i.deleted = false where i.id = ?1 and i.deleted = true")
    void restore(UUID id);
}
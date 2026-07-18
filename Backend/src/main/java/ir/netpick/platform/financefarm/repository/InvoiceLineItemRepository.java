package ir.netpick.platform.financefarm.repository;

import ir.netpick.platform.financefarm.model.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, UUID> {
    List<InvoiceLineItem> findByInvoiceIdAndDeletedFalse(UUID invoiceId);

    // Soft delete
    @Transactional
    @Modifying
    @Query("update InvoiceLineItem i set i.deleted = true where i.deleted = false and i.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update InvoiceLineItem i set i.deleted = false where i.id = ?1 and i.deleted = true")
    void restore(UUID id);
}
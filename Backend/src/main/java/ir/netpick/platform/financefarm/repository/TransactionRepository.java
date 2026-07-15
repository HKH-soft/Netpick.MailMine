package ir.netpick.platform.financefarm.repository;

import ir.netpick.platform.financefarm.model.Transaction;
import ir.netpick.platform.financefarm.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Find all non-deleted transactions with pagination
    Page<Transaction> findByDeletedFalse(Pageable pageable);

    // Find transactions by type with pagination
    Page<Transaction> findByTypeAndDeletedFalse(TransactionType type, Pageable pageable);

    // Find transactions by date range
    @Query("select t from Transaction t where t.deleted = false and t.date between ?1 and ?2")
    List<Transaction> findByDateBetweenAndDeletedFalse(LocalDateTime startDate, LocalDateTime endDate);

    // Find transactions by creator with pagination
    Page<Transaction> findByCreatedByIdAndDeletedFalse(UUID createdBy, Pageable pageable);

    // Sum amount by type
    @Query("select sum(t.amount) from Transaction t where t.deleted = false and t.type = ?1")
    BigDecimal sumAmountByType(TransactionType type);

    // Soft delete
    @Transactional
    @Modifying
    @Query("update Transaction t set t.deleted = true where t.deleted = false and t.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update Transaction t set t.deleted = false where t.id = ?1 and t.deleted = true")
    void restore(UUID id);
}
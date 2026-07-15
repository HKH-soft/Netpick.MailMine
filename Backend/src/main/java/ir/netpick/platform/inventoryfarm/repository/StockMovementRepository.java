package ir.netpick.platform.inventoryfarm.repository;

import ir.netpick.platform.inventoryfarm.model.StockMovement;
import ir.netpick.platform.inventoryfarm.model.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    // Find all non-deleted movements with pagination
    Page<StockMovement> findByDeletedFalse(Pageable pageable);

    // Find movements by product with pagination
    Page<StockMovement> findByProductIdAndDeletedFalse(UUID productId, Pageable pageable);

    // Find movements by date range
    @Query("select sm from StockMovement sm where sm.deleted = false and sm.movementDate between ?1 and ?2")
    List<StockMovement> findByDateBetweenAndDeletedFalse(LocalDateTime startDate, LocalDateTime endDate);

    // Soft delete
    @Transactional
    @Modifying
    @Query("update StockMovement sm set sm.deleted = true where sm.deleted = false and sm.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update StockMovement sm set sm.deleted = false where sm.id = ?1 and sm.deleted = true")
    void restore(UUID id);
}
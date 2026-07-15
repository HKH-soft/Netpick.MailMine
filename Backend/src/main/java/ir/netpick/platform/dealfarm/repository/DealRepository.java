package ir.netpick.platform.dealfarm.repository;

import ir.netpick.platform.dealfarm.model.Deal;
import ir.netpick.platform.dealfarm.model.DealStage;
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
public interface DealRepository extends JpaRepository<Deal, UUID> {

    // Find all non-deleted deals with pagination
    Page<Deal> findByDeletedFalse(Pageable pageable);

    // Count all non-deleted deals
    long countByDeletedFalse();

    // Find deals by stage with pagination
    Page<Deal> findByStageAndDeletedFalse(DealStage stage, Pageable pageable);

    // Find deals by owner with pagination
    Page<Deal> findByOwnerIdAndDeletedFalse(UUID ownerId, Pageable pageable);

    // Count deals by stage
    @Query("select count(d) from Deal d where d.deleted = false and d.stage = ?1")
    long countByStageAndDeletedFalse(DealStage stage);

    // Sum value by stage
    @Query("select sum(d.value) from Deal d where d.deleted = false and d.stage = ?1")
    BigDecimal sumValueByStage(DealStage stage);

    // Soft delete
    @Transactional
    @Modifying
    @Query("update Deal d set d.deleted = true where d.deleted = false and d.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update Deal d set d.deleted = false where d.id = ?1 and d.deleted = true")
    void restore(UUID id);
}
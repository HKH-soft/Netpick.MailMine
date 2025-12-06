package ir.netpick.mailmine.scrape.repository;

import ir.netpick.mailmine.common.enums.PipelineStateEnum;
import ir.netpick.mailmine.scrape.model.Pipeline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {
    long countByDeletedFalse();

    long countByStateAndDeletedFalse(PipelineStateEnum state);

    @Query("SELECT SUM(p.contactsFound) FROM Pipeline p WHERE p.deleted = false")
    Long sumContactsFound();

    // Find all non-deleted pipelines with pagination
    Page<Pipeline> findByDeletedFalse(Pageable pageable);

    // Find all deleted pipelines with pagination
    Page<Pipeline> findByDeletedTrue(Pageable pageable);

    @Transactional
    @Modifying
    @Query("update Pipeline c set c.deleted = True where c.deleted = false and c.id = ?1")
    void softDelete(UUID id);

    @Transactional
    @Modifying
    @Query("update Pipeline c set c.deleted = False where c.id = ?1 and c.deleted = true")
    void restore(UUID id);
}
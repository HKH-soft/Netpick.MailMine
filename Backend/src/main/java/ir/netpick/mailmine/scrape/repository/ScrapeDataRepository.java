package ir.netpick.mailmine.scrape.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ir.netpick.mailmine.scrape.model.ScrapeData;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ScrapeDataRepository extends JpaRepository<ScrapeData, UUID> {

    // DEPRECATED: Use findByParsedFalseAndDeletedFalse with pagination instead
    @Deprecated
    List<ScrapeData> findByParsedFalse();

    /**
     * Find unparsed files with pagination to avoid OOM
     */
    Page<ScrapeData> findByParsedFalseAndDeletedFalse(Pageable pageable);

    /**
     * Count unparsed files for progress tracking
     */
    long countByParsedFalseAndDeletedFalse();

    // Find all non-deleted data with pagination
    Page<ScrapeData> findByDeletedFalse(Pageable pageable);

    // Find all deleted data with pagination
    Page<ScrapeData> findByDeletedTrue(Pageable pageable);

    @Transactional
    @Modifying
    @Query("update ScrapeData s set s.deleted = True where s.deleted = false and s.id = ?1")
    void softDelete(UUID id);

    @Transactional
    @Modifying
    @Query("update ScrapeData s set s.deleted = False where s.deleted = true and s.id = ?1")
    void restore(UUID id);
}
package ir.netpick.mailmine.scrape.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.netpick.mailmine.scrape.model.ScrapeJob;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ScrapeJobRepository extends JpaRepository<ScrapeJob, UUID> {
    boolean existsByLink(String link);

    Set<ScrapeJob> findAllByLinkIn(List<String> links);

    Optional<ScrapeJob> findByLink(String link);

    // DEPRECATED: Use findPendingJobs instead
    @Deprecated
    List<ScrapeJob> findByAttemptLessThanEqual(int attempt);

    /**
     * Find jobs that haven't been scraped yet, haven't permanently failed,
     * and are under the max attempt limit.
     * This is the correct query for fetching pending jobs.
     */
    @Query("SELECT j FROM ScrapeJob j WHERE j.beenScraped = false AND j.scrapeFailed = false AND j.attempt < :maxAttempts AND j.deleted = false")
    List<ScrapeJob> findPendingJobs(@Param("maxAttempts") int maxAttempts);

    /**
     * Find pending jobs with pagination to avoid memory issues
     */
    @Query("SELECT j FROM ScrapeJob j WHERE j.beenScraped = false AND j.scrapeFailed = false AND j.attempt < :maxAttempts AND j.deleted = false")
    Page<ScrapeJob> findPendingJobs(@Param("maxAttempts") int maxAttempts, Pageable pageable);

    /**
     * Count pending jobs for progress tracking
     */
    @Query("SELECT COUNT(j) FROM ScrapeJob j WHERE j.beenScraped = false AND j.scrapeFailed = false AND j.attempt < :maxAttempts AND j.deleted = false")
    long countPendingJobs(@Param("maxAttempts") int maxAttempts);

    // Find all non-deleted jobs (this will be the new default)
    List<ScrapeJob> findByDeletedFalse();

    // Find all non-deleted jobs with pagination
    Page<ScrapeJob> findByDeletedFalse(Pageable pageable);

    // Find all deleted jobs with pagination
    Page<ScrapeJob> findByDeletedTrue(Pageable pageable);

    @Transactional
    @Modifying
    @Query("update ScrapeJob s set s.deleted = True where s.deleted = false and s.id = ?1")
    void softDelete(UUID id);

    @Transactional
    @Modifying
    @Query("update ScrapeJob s set s.deleted = False where s.deleted = true and s.id = ?1")
    void restore(UUID id);
}
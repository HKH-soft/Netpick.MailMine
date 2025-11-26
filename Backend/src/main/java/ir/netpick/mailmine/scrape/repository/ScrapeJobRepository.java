package ir.netpick.mailmine.scrape.repository;

import ir.netpick.mailmine.scrape.model.ScrapeJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ScrapeJobRepository extends JpaRepository<ScrapeJob, UUID> {
    boolean existsByLink(String link);

    Set<ScrapeJob> findAllByLinkIn(List<String> links);

    Optional<ScrapeJob> findByLink(String link);

    List<ScrapeJob> findByAttemptLessThanEqual(int attempt);

    @Transactional
    @Modifying
    @Query("update ScrapeJob s set s.deleted = True where s.deleted = false and s.id = ?1")
    void softDelete(UUID id);

    @Transactional
    @Modifying
    @Query("update ScrapeJob s set s.deleted = False where s.deleted = true and s.id = ?1")
    void restore(UUID id);
}

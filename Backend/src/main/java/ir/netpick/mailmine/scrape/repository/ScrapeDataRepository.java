package ir.netpick.mailmine.scrape.repository;

import ir.netpick.mailmine.scrape.model.ScrapeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Repository
public interface ScrapeDataRepository extends JpaRepository<ScrapeData, UUID> {
    List<ScrapeData> findByParsedFalse();


    @Transactional
    @Modifying
    @Query("update ScrapeData s set s.deleted = True where s.deleted = false and s.id = ?1")
    void softDelete(UUID id);

    @Transactional
    @Modifying
    @Query("update ScrapeData s set s.deleted = False where s.deleted = true and s.id = ?1")
    void restore(UUID id);
}

package ir.netpick.mailmine.scrape.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import ir.netpick.mailmine.scrape.model.SearchQuery;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SearchQueryRepository extends JpaRepository<SearchQuery, UUID> {

    @Transactional
    @Modifying
    @Query("update SearchQuery s set s.deleted = True where s.deleted = false and s.id = ?1")
    void softDelete(UUID id);

    @Transactional
    @Modifying
    @Query("update SearchQuery s set s.deleted = False where s.deleted = true and s.id = ?1")
    void restore(UUID id);

    List<SearchQuery> findByLinkCountLessThan(@NonNull Integer link_count);
}

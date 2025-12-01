package ir.netpick.mailmine.scrape.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ir.netpick.mailmine.scrape.model.ApiKey;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    // Find all non-deleted api keys with pagination
    Page<ApiKey> findByDeletedFalse(Pageable pageable);
    
    // Find all deleted api keys with pagination
    Page<ApiKey> findByDeletedTrue(Pageable pageable);

    @Transactional
    @Modifying
    @Query("update ApiKey s set s.deleted = True where s.deleted = false and s.id = ?1")
    void softDelete(UUID id);

    @Transactional
    @Modifying
    @Query("update ApiKey s set s.deleted = False where s.deleted = true and s.id = ?1")
    void restore(UUID id);
}
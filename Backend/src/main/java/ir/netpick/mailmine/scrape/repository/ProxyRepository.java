package ir.netpick.mailmine.scrape.repository;

import ir.netpick.mailmine.common.enums.ProxyStatus;
import ir.netpick.mailmine.scrape.model.Proxy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProxyRepository extends JpaRepository<Proxy, UUID> {

    Page<Proxy> findByDeletedFalse(Pageable pageable);

    Page<Proxy> findByDeletedTrue(Pageable pageable);

    List<Proxy> findByStatusAndDeletedFalse(ProxyStatus status);

    List<Proxy> findByStatusInAndDeletedFalse(List<ProxyStatus> statuses);

    Optional<Proxy> findByHostAndPort(String host, Integer port);

    boolean existsByHostAndPort(String host, Integer port);

    @Query("SELECT p FROM Proxy p WHERE p.status IN :statuses AND p.deleted = false ORDER BY p.avgResponseTimeMs ASC NULLS LAST, p.successCount DESC")
    List<Proxy> findBestProxies(List<ProxyStatus> statuses);

    @Query("SELECT p FROM Proxy p WHERE p.status = 'UNTESTED' AND p.deleted = false")
    List<Proxy> findUntestedProxies();

    @Modifying
    @Transactional
    @Query("UPDATE Proxy p SET p.deleted = true WHERE p.id = :id")
    void softDelete(UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Proxy p SET p.deleted = false WHERE p.id = :id")
    void restore(UUID id);

    long countByStatusAndDeletedFalse(ProxyStatus status);
}

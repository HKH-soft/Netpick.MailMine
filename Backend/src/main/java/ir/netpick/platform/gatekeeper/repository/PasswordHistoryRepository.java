package ir.netpick.platform.gatekeeper.repository;

import ir.netpick.platform.gatekeeper.model.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user.id = :userId ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findRecentByUserId(UUID userId);

    @Query("SELECT ph.passwordHash FROM PasswordHistory ph WHERE ph.user.id = :userId ORDER BY ph.createdAt DESC")
    List<String> findRecentHashesByUserId(UUID userId);
}

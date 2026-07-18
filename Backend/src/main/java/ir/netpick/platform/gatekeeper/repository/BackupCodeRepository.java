package ir.netpick.platform.gatekeeper.repository;

import ir.netpick.platform.gatekeeper.model.BackupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BackupCodeRepository extends JpaRepository<BackupCode, UUID> {

    List<BackupCode> findByUserIdAndUsedFalse(UUID userId);

    @Modifying
    @Query("DELETE FROM BackupCode b WHERE b.user.id = :userId")
    void deleteAllByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM BackupCode b WHERE b.user.id = :userId AND b.used = true")
    int deleteUsedByUserId(UUID userId);

    long countByUserIdAndUsedFalse(UUID userId);
}

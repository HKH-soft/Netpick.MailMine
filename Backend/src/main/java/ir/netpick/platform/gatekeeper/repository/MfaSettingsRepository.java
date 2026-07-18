package ir.netpick.platform.gatekeeper.repository;

import ir.netpick.platform.gatekeeper.model.MfaSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MfaSettingsRepository extends JpaRepository<MfaSettings, UUID> {

    Optional<MfaSettings> findByUserId(UUID userId);

    boolean existsByUserIdAndMfaEnabledTrue(UUID userId);
}

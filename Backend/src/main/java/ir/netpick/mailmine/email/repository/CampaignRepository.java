package ir.netpick.mailmine.email.repository;

import ir.netpick.mailmine.email.model.Campaign;
import ir.netpick.mailmine.email.model.Campaign.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    List<Campaign> findByStatus(CampaignStatus status);

    List<Campaign> findByCreatedById(UUID userId);

    List<Campaign> findByStatusAndScheduledAtBefore(CampaignStatus status, java.time.LocalDateTime now);

    List<Campaign> findByNameContainingIgnoreCase(String name);
}

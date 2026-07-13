package ir.netpick.platform.mailmine.repository;

import ir.netpick.platform.mailmine.model.CampaignRecipient;
import ir.netpick.platform.mailmine.model.CampaignRecipient.RecipientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignRecipientRepository extends JpaRepository<CampaignRecipient, UUID> {

    List<CampaignRecipient> findByCampaignId(UUID campaignId);

    List<CampaignRecipient> findByCampaignIdAndStatus(UUID campaignId, RecipientStatus status);

    @Query("SELECT COUNT(cr) FROM CampaignRecipient cr WHERE cr.campaign.id = :campaignId AND cr.status = :status")
    long countByCampaignIdAndStatus(@Param("campaignId") UUID campaignId, @Param("status") RecipientStatus status);
}










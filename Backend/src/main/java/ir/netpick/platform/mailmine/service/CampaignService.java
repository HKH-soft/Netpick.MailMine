package ir.netpick.platform.mailmine.service;

import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.mailmine.dto.EmailRequest;
import ir.netpick.platform.mailmine.model.Campaign;
import ir.netpick.platform.mailmine.model.CampaignRecipient;
import ir.netpick.platform.mailmine.repository.CampaignRecipientRepository;
import ir.netpick.platform.mailmine.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignRecipientRepository campaignRecipientRepository;
    private final EmailQueueService emailQueueService;

    public Page<Campaign> listAll(Pageable pageable) {
        return campaignRepository.findAll(pageable);
    }

    public Campaign getById(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
    }

    public Campaign create(Campaign campaign) {
        campaign.setStatus(Campaign.CampaignStatus.DRAFT);
        return campaignRepository.save(campaign);
    }

    public Campaign update(UUID id, Campaign updates) {
        Campaign existing = getById(id);
        if (existing.getStatus() != Campaign.CampaignStatus.DRAFT) {
            throw new IllegalStateException("Can only update draft campaigns");
        }
        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());
        existing.setSubjectLine(updates.getSubjectLine());
        existing.setBodyHtml(updates.getBodyHtml());
        existing.setBodyText(updates.getBodyText());
        return campaignRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        Campaign campaign = getById(id);
        if (campaign.getStatus() != Campaign.CampaignStatus.DRAFT) {
            throw new IllegalStateException("Can only delete draft campaigns");
        }
        campaign.setDeleted(true);
        campaignRepository.save(campaign);
    }

    @Transactional
    public Campaign addRecipients(UUID campaignId, List<String> emails) {
        Campaign campaign = getById(campaignId);
        if (campaign.getStatus() != Campaign.CampaignStatus.DRAFT) {
            throw new IllegalStateException("Can only add recipients to draft campaigns");
        }

        int added = 0;
        for (String email : emails) {
            CampaignRecipient recipient = new CampaignRecipient();
            recipient.setCampaign(campaign);
            recipient.setRecipientEmail(email);
            recipient.setStatus(CampaignRecipient.RecipientStatus.PENDING);
            campaignRecipientRepository.save(recipient);
            added++;
        }

        campaign.setTotalRecipients(campaign.getTotalRecipients() + added);
        return campaignRepository.save(campaign);
    }

    public Campaign schedule(UUID campaignId, LocalDateTime scheduledAt) {
        Campaign campaign = getById(campaignId);
        if (campaign.getTotalRecipients() == 0) {
            throw new IllegalStateException("Cannot schedule campaign with no recipients");
        }
        campaign.setScheduledAt(scheduledAt);
        campaign.setStatus(Campaign.CampaignStatus.SCHEDULED);
        return campaignRepository.save(campaign);
    }

    @Transactional
    public Campaign sendNow(UUID campaignId) {
        Campaign campaign = getById(campaignId);
        campaign.setStatus(Campaign.CampaignStatus.SENDING);
        campaign.setSentAt(LocalDateTime.now());
        campaignRepository.save(campaign);

        List<CampaignRecipient> recipients = campaignRecipientRepository
                .findByCampaignIdAndStatus(campaignId, CampaignRecipient.RecipientStatus.PENDING);

        int sentCount = 0;
        for (CampaignRecipient recipient : recipients) {
            try {
                EmailRequest request = EmailRequest.builder()
                        .recipient(recipient.getRecipientEmail())
                        .subject(campaign.getSubjectLine())
                        .body(campaign.getBodyHtml())
                        .build();

                emailQueueService.queueEmail(request, campaign.getCreatedBy().getId());
                recipient.setStatus(CampaignRecipient.RecipientStatus.SENT);
                recipient.setSentAt(LocalDateTime.now());
                campaignRecipientRepository.save(recipient);
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send campaign email to {}: {}", recipient.getRecipientEmail(), e.getMessage());
                recipient.setStatus(CampaignRecipient.RecipientStatus.FAILED);
                campaignRecipientRepository.save(recipient);
            }
        }

        campaign.setTotalSent(sentCount);
        campaign.setStatus(Campaign.CampaignStatus.SENT);
        return campaignRepository.save(campaign);
    }

    /**
     * Process scheduled campaigns - runs every minute
     */
    @Scheduled(fixedDelay = 60000)
    public void processScheduledCampaigns() {
        List<Campaign> scheduled = campaignRepository
                .findByStatusAndScheduledAtBefore(Campaign.CampaignStatus.SCHEDULED, LocalDateTime.now());

        for (Campaign campaign : scheduled) {
            log.info("Sending scheduled campaign: {}", campaign.getName());
            sendNow(campaign.getId());
        }
    }

    public List<CampaignRecipient> getRecipients(UUID campaignId) {
        return campaignRecipientRepository.findByCampaignId(campaignId);
    }

    public Campaign getCampaignStats(UUID campaignId) {
        Campaign campaign = getById(campaignId);
        campaign.setTotalSent((int) campaignRecipientRepository.countByCampaignIdAndStatus(
                campaignId, CampaignRecipient.RecipientStatus.SENT));
        campaign.setTotalOpened((int) campaignRecipientRepository.countByCampaignIdAndStatus(
                campaignId, CampaignRecipient.RecipientStatus.OPENED));
        campaign.setTotalClicked((int) campaignRecipientRepository.countByCampaignIdAndStatus(
                campaignId, CampaignRecipient.RecipientStatus.CLICKED));
        campaign.setTotalBounced((int) campaignRecipientRepository.countByCampaignIdAndStatus(
                campaignId, CampaignRecipient.RecipientStatus.BOUNCED));
        campaign.setTotalUnsubscribed((int) campaignRecipientRepository.countByCampaignIdAndStatus(
                campaignId, CampaignRecipient.RecipientStatus.UNSUBSCRIBED));
        return campaignRepository.save(campaign);
    }
}










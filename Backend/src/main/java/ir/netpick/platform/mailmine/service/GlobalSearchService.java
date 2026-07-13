package ir.netpick.platform.mailmine.service;

import ir.netpick.platform.mailmine.model.EmailMessage;
import ir.netpick.platform.mailmine.model.EmailTag;
import ir.netpick.platform.mailmine.model.SharedInbox;
import ir.netpick.platform.mailmine.model.Campaign;
import ir.netpick.platform.mailmine.repository.EmailMessageRepository;
import ir.netpick.platform.mailmine.repository.EmailTagRepository;
import ir.netpick.platform.mailmine.repository.SharedInboxRepository;
import ir.netpick.platform.mailmine.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailTagRepository emailTagRepository;
    private final SharedInboxRepository sharedInboxRepository;
    private final CampaignRepository campaignRepository;

    public Map<String, Object> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> results = new LinkedHashMap<>();

        // Search emails by subject, sender
        List<EmailMessage> emails = emailMessageRepository
                .findBySubjectContainingIgnoreCaseOrSenderEmailContainingIgnoreCase(query, query, pageable)
                .getContent();
        results.put("emails", emails.stream().map(e -> Map.of(
                "id", e.getId(),
                "type", "email",
                "title", e.getSubject() != null ? e.getSubject() : "(no subject)",
                "subtitle", e.getSenderEmail(),
                "date", e.getReceivedAt().toString()
        )).collect(Collectors.toList()));

        // Search tags
        List<EmailTag> tags = emailTagRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        results.put("tags", tags.stream().map(t -> Map.of(
                "id", t.getId(),
                "type", "tag",
                "title", t.getName(),
                "subtitle", t.getCategory()
        )).collect(Collectors.toList()));

        // Search shared inboxes
        List<SharedInbox> inboxes = sharedInboxRepository
                .findByNameContainingIgnoreCaseOrEmailAddressContainingIgnoreCase(query, query);
        results.put("sharedInboxes", inboxes.stream().map(i -> Map.of(
                "id", i.getId(),
                "type", "sharedInbox",
                "title", i.getName(),
                "subtitle", i.getEmailAddress()
        )).collect(Collectors.toList()));

        // Search campaigns
        List<Campaign> campaigns = campaignRepository
                .findByNameContainingIgnoreCase(query);
        results.put("campaigns", campaigns.stream().map(c -> Map.of(
                "id", c.getId(),
                "type", "campaign",
                "title", c.getName(),
                "subtitle", c.getStatus().toString()
        )).collect(Collectors.toList()));

        // Compute totals
        int total = emails.size() + tags.size() + inboxes.size() + campaigns.size();
        results.put("totalResults", total);

        return results;
    }
}










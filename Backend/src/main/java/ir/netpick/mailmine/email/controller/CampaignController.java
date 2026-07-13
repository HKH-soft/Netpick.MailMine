package ir.netpick.mailmine.email.controller;

import ir.netpick.mailmine.email.model.Campaign;
import ir.netpick.mailmine.email.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public ResponseEntity<Page<Campaign>> listCampaigns(Pageable pageable) {
        return ResponseEntity.ok(campaignService.listAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campaign> getCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Campaign> createCampaign(@RequestBody Campaign campaign) {
        return ResponseEntity.ok(campaignService.create(campaign));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Campaign> updateCampaign(
            @PathVariable UUID id,
            @RequestBody Campaign campaign) {
        return ResponseEntity.ok(campaignService.update(id, campaign));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable UUID id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/recipients")
    public ResponseEntity<Campaign> addRecipients(
            @PathVariable UUID id,
            @RequestBody List<String> emails) {
        return ResponseEntity.ok(campaignService.addRecipients(id, emails));
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Campaign> scheduleCampaign(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt) {
        return ResponseEntity.ok(campaignService.schedule(id, scheduledAt));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<Campaign> sendNow(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.sendNow(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Campaign> getCampaignStats(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.getCampaignStats(id));
    }

    @GetMapping("/{id}/recipients")
    public ResponseEntity<?> getCampaignRecipients(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.getRecipients(id));
    }
}

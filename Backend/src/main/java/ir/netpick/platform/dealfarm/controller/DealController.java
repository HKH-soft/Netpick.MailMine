package ir.netpick.platform.dealfarm.controller;

import ir.netpick.platform.dealfarm.dto.DealDTO;
import ir.netpick.platform.dealfarm.model.Deal;
import ir.netpick.platform.dealfarm.model.DealStage;
import ir.netpick.platform.dealfarm.service.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * DealFarm - CRM and Sales Pipeline Controller
 */
@RestController
@RequestMapping("/api/v1/dealfarm/deals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class DealController {

    private final DealService dealService;

    @GetMapping
    public ResponseEntity<?> getAllDeals(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(dealService.getAll(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeal(@PathVariable UUID id) {
        return ResponseEntity.ok(dealService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> createDeal(@RequestBody DealDTO request) {
        Deal deal = new Deal();
        deal.setTitle(request.title());
        deal.setDescription(request.description());
        deal.setStage(DealStage.valueOf(request.stage()));
        deal.setValue(java.math.BigDecimal.valueOf(request.value()));
        deal.setCurrency(request.currency());
        deal.setContactId(request.contactId());
        deal.setOwnerId(request.ownerId());
        deal.setProbability(request.probability());
        deal.setExpectedCloseDate(request.expectedCloseDate());
        return ResponseEntity.ok(dealService.create(deal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDeal(@PathVariable UUID id, @RequestBody DealDTO request) {
        Deal deal = new Deal();
        deal.setTitle(request.title());
        deal.setDescription(request.description());
        deal.setStage(DealStage.valueOf(request.stage()));
        deal.setValue(java.math.BigDecimal.valueOf(request.value()));
        deal.setCurrency(request.currency());
        deal.setContactId(request.contactId());
        deal.setOwnerId(request.ownerId());
        deal.setProbability(request.probability());
        deal.setExpectedCloseDate(request.expectedCloseDate());
        return ResponseEntity.ok(dealService.update(id, deal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeal(@PathVariable UUID id) {
        dealService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreDeal(@PathVariable UUID id) {
        dealService.restore(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stage/{stage}")
    public ResponseEntity<?> getByStage(@PathVariable String stage, @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(dealService.getByStage(DealStage.valueOf(stage), page));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(dealService.getStats());
    }
}
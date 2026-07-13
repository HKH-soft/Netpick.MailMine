package ir.netpick.platform.dealfarm.controller;

import ir.netpick.platform.dealfarm.dto.DealDTO;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * DealFarm - CRM and Sales Pipeline Controller
 */
@RestController
@RequestMapping("/api/v1/dealfarm/deals")
@RequiredArgsConstructor
public class DealController {

    // TODO: Add DealService dependency

    @GetMapping
    public ResponseEntity<List<DealDTO>> getAllDeals(@AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealDTO> getDeal(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(null);
    }

    @PostMapping
    public ResponseEntity<DealDTO> createDeal(@RequestBody DealDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealDTO> updateDeal(@PathVariable UUID id, @RequestBody DealDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeal(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
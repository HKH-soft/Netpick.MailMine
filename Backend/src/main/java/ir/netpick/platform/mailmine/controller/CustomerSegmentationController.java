package ir.netpick.platform.mailmine.controller;

import ir.netpick.platform.mailmine.service.CustomerSegmentationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mailmine/segments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class CustomerSegmentationController {

    private final CustomerSegmentationService segmentationService;

    @GetMapping("/activity")
    public ResponseEntity<Map<String, Object>> segmentByActivity() {
        return ResponseEntity.ok(segmentationService.segmentByActivity());
    }

    @GetMapping("/top-customers")
    public ResponseEntity<List<Map<String, Object>>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(segmentationService.getTopCustomers(limit));
    }
}










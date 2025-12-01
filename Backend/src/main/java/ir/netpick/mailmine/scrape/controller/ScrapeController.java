package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.common.enums.PipelineStageEnum;
import ir.netpick.mailmine.scrape.model.Pipeline;
import ir.netpick.mailmine.scrape.service.orch.ScrapeOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/scrape")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ScrapeController {

    private final ScrapeOrchestrationService orchestrationService;

    @PostMapping("start_google")
    public ResponseEntity<Map<String, String>> startSearch() {
        if (orchestrationService.hasActivePipeline()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A pipeline is already running"));
        }
        orchestrationService.executeSteps(Set.of(PipelineStageEnum.API_CALLER_STARTED));
        return ResponseEntity.accepted()
                .body(Map.of("message", "Google search started"));
    }

    @PostMapping("start_scrape")
    public ResponseEntity<Map<String, String>> startScrapping() {
        if (orchestrationService.hasActivePipeline()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A pipeline is already running"));
        }
        orchestrationService.executeSteps(Set.of(PipelineStageEnum.SCRAPER_STARTED));
        return ResponseEntity.accepted()
                .body(Map.of("message", "Scraping started"));
    }

    @PostMapping("start_extract")
    public ResponseEntity<Map<String, String>> startExtract() {
        if (orchestrationService.hasActivePipeline()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A pipeline is already running"));
        }
        orchestrationService.executeSteps(Set.of(PipelineStageEnum.PARSER_STARTED));
        return ResponseEntity.accepted()
                .body(Map.of("message", "Extraction started"));
    }

    @PostMapping("execute_steps")
    public ResponseEntity<Map<String, String>> executeSteps(@RequestBody Set<PipelineStageEnum> steps) {
        if (orchestrationService.hasActivePipeline()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A pipeline is already running"));
        }
        orchestrationService.executeSteps(steps);
        return ResponseEntity.accepted()
                .body(Map.of("message", "Pipeline started with steps: " + steps));
    }

    @PostMapping("execute_all")
    public ResponseEntity<Map<String, String>> executeAll() {
        if (orchestrationService.hasActivePipeline()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A pipeline is already running"));
        }
        orchestrationService.executeAllSteps();
        return ResponseEntity.accepted()
                .body(Map.of("message", "Full pipeline started"));
    }

    // ==================== Pipeline Control Endpoints ====================

    @PostMapping("pause")
    public ResponseEntity<Pipeline> pausePipeline() {
        Pipeline pipeline = orchestrationService.pauseCurrentPipeline();
        return ResponseEntity.ok(pipeline);
    }

    @PostMapping("resume")
    public ResponseEntity<Pipeline> resumePipeline() {
        Pipeline pipeline = orchestrationService.resumeCurrentPipeline();
        return ResponseEntity.ok(pipeline);
    }

    @PostMapping("skip")
    public ResponseEntity<Pipeline> skipCurrentStep() {
        Pipeline pipeline = orchestrationService.skipCurrentStep();
        return ResponseEntity.ok(pipeline);
    }

    @PostMapping("cancel")
    public ResponseEntity<Pipeline> cancelPipeline() {
        Pipeline pipeline = orchestrationService.cancelCurrentPipeline();
        return ResponseEntity.ok(pipeline);
    }

    @GetMapping("status")
    public ResponseEntity<Map<String, Object>> getPipelineStatus() {
        boolean active = orchestrationService.hasActivePipeline();
        return ResponseEntity.ok(Map.of(
                "active", active,
                "message", active ? "Pipeline is running" : "No active pipeline"));
    }
}
package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.service.base.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/scrape/pipelines")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class PipelineController {
    private final PipelineService pipelineService;

    @GetMapping
    public ResponseEntity<?> allPipelines(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok()
                .body(pipelineService.allPipelines(page));
    }

    @GetMapping("/deleted")
    public ResponseEntity<?> deletedPipelines(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok()
                .body(pipelineService.deletedPipelines(page));
    }

    @GetMapping("/all")
    public ResponseEntity<?> allPipelinesIncludingDeleted(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok()
                .body(pipelineService.allPipelinesIncludingDeleted(page));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getPipeline(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .body(pipelineService.getPipeline(id));
    }

    @GetMapping("/deleted/{id}")
    public ResponseEntity<?> getDeletedPipeline(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .body(pipelineService.getPipelineIncludingDeleted(id));
    }

    @PutMapping("{id}/restore")
    public ResponseEntity<?> restorePipeline(@PathVariable UUID id) {
        pipelineService.restorePipeline(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> softDeletePipeline(@PathVariable UUID id) {
        pipelineService.softDeletePipeline(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{id}/full_delete")
    public ResponseEntity<?> fullDeletePipeline(@PathVariable UUID id) {
        pipelineService.deletePipeline(pipelineService.getPipeline(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(pipelineService.getStats());
    }
}
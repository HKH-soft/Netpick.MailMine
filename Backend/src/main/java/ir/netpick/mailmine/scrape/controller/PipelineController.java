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
    public ResponseEntity<?> allPipelines(@RequestParam(defaultValue = "1") int page){
        return ResponseEntity.ok()
                .body(pipelineService.allPipelines(page));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getPipeline(@PathVariable UUID id){
        return ResponseEntity.ok()
                .body(pipelineService.getPipeline(id));
    }
}

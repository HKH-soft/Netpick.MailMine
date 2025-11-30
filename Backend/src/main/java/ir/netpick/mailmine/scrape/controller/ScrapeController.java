package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.common.enums.PipelineStageEnum;
import ir.netpick.mailmine.scrape.service.orch.ScrapeOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/scrape")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ScrapeController {

    private final ScrapeOrchestrationService orchestrationService;
    @PostMapping("start_google")
    public void startSearch() {
        orchestrationService.executeSteps(Set.of(PipelineStageEnum.API_CALLER_STARTED));
    }

    @PostMapping("start_scrape")
    public void startScrapping() {
        orchestrationService.executeSteps(Set.of(PipelineStageEnum.SCRAPER_STARTED));
    }

    @PostMapping("start_extract")
    public void startExtract() {
        orchestrationService.executeSteps(Set.of(PipelineStageEnum.PARSER_STARTED));
    }

    @PostMapping("execute_steps")
    public void executeSteps(@RequestBody Set<PipelineStageEnum> steps) {
        orchestrationService.executeSteps(steps);
    }

    @PostMapping("execute_all")
    public void executeAll() {
        orchestrationService.executeAllSteps();
    }

}
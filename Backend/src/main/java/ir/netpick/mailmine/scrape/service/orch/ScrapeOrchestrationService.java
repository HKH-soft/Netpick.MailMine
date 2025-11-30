package ir.netpick.mailmine.scrape.service.orch;

import ir.netpick.mailmine.common.enums.PipelineStageEnum;
import ir.netpick.mailmine.common.enums.PipelineStateEnum;
import ir.netpick.mailmine.scrape.model.Pipeline;
import ir.netpick.mailmine.scrape.service.base.PipelineService;
import ir.netpick.mailmine.scrape.service.mid.ApiCaller;
import ir.netpick.mailmine.scrape.service.mid.DataProcessor;
import ir.netpick.mailmine.scrape.service.mid.Scraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapeOrchestrationService {

    private final ApiCaller apiCaller;
    private final Scraper scraper;
    private final DataProcessor dataProcessor;
    private final PipelineService pipelineService;

    /**
     * Execute selected steps of the scraping pipeline asynchronously
     * Steps run sequentially within the async context
     *
     * @param steps Set of steps to execute
     */
    @Async
    public void executeSteps(Set<PipelineStageEnum> steps) {
        Pipeline pipeline = new Pipeline(PipelineStageEnum.STARTED, PipelineStateEnum.RUNNING, LocalDateTime.now());
        pipeline = pipelineService.createPipeline(pipeline);

        try {
            if (steps.contains(PipelineStageEnum.API_CALLER_STARTED)) {
                log.info("Executing API caller step");
                apiCaller.callGoogleSearch(); // Call directly, remove async wrapper
                pipeline.setStage(PipelineStageEnum.API_CALLER_COMPLETE);
                pipelineService.updatePipeline(pipeline);
            }

            if (steps.contains(PipelineStageEnum.SCRAPER_STARTED)) {
                log.info("Executing scraper step");
                scraper.scrapePendingJobs(); // Call directly, remove async wrapper
                pipeline.setStage(PipelineStageEnum.SCRAPER_COMPLETE);
                pipelineService.updatePipeline(pipeline);
            }

            if (steps.contains(PipelineStageEnum.PARSER_STARTED)) {
                log.info("Executing data processor step");
                dataProcessor.processUnparsedFiles();
                pipeline.setStage(PipelineStageEnum.PARSER_COMPLETE);
                pipelineService.updatePipeline(pipeline);
            }

            pipeline.setState(PipelineStateEnum.COMPLETED);
            pipeline.setEndTime(LocalDateTime.now());
            pipelineService.updatePipeline(pipeline);
            log.info("Selected steps executed successfully");

        } catch (Exception e) {
            log.error("Error during orchestrated scraping", e);
            pipeline.setState(PipelineStateEnum.FAILED);
            pipeline.setEndTime(LocalDateTime.now());
            pipelineService.updatePipeline(pipeline);
        }
    }

    /**
     * Execute all steps in sequence
     */
    public void executeAllSteps() {
        executeSteps(EnumSet.allOf(PipelineStageEnum.class));
    }
}
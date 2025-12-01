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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapeOrchestrationService {

    private final ApiCaller apiCaller;
    private final Scraper scraper;
    private final DataProcessor dataProcessor;
    private final PipelineService pipelineService;
    private final PipelineControlService pipelineControlService;

    /**
     * Execute selected steps of the scraping pipeline asynchronously
     * Steps run sequentially within the async context
     *
     * @param steps Set of steps to execute
     * @return The pipeline ID for tracking
     */
    @Async
    public void executeSteps(Set<PipelineStageEnum> steps) {
        Pipeline pipeline = new Pipeline(PipelineStageEnum.STARTED, PipelineStateEnum.RUNNING, LocalDateTime.now());
        pipeline = pipelineService.createPipeline(pipeline);
        UUID pipelineId = pipeline.getId();

        // Register pipeline for control
        pipelineControlService.registerPipeline(pipelineId);

        try {
            // API Caller Step
            if (steps.contains(PipelineStageEnum.API_CALLER_STARTED)) {
                if (!executeStep(pipelineId, pipeline, PipelineStageEnum.API_CALLER_STARTED,
                        PipelineStageEnum.API_CALLER_COMPLETE, () -> {
                            log.info("Executing API caller step");
                            apiCaller.callGoogleSearch();
                        })) {
                    return; // Cancelled
                }
            }

            // Scraper Step
            if (steps.contains(PipelineStageEnum.SCRAPER_STARTED)) {
                if (!executeStep(pipelineId, pipeline, PipelineStageEnum.SCRAPER_STARTED,
                        PipelineStageEnum.SCRAPER_COMPLETE, () -> {
                            log.info("Executing scraper step");
                            scraper.scrapePendingJobs();
                        })) {
                    return; // Cancelled
                }
            }

            // Parser Step
            if (steps.contains(PipelineStageEnum.PARSER_STARTED)) {
                if (!executeStep(pipelineId, pipeline, PipelineStageEnum.PARSER_STARTED,
                        PipelineStageEnum.PARSER_COMPLETE, () -> {
                            log.info("Executing data processor step");
                            dataProcessor.processUnparsedFiles();
                        })) {
                    return; // Cancelled
                }
            }

            pipeline.setState(PipelineStateEnum.COMPLETED);
            pipeline.setEndTime(LocalDateTime.now());
            pipelineService.updatePipeline(pipeline);
            log.info("Pipeline {} completed successfully", pipelineId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Pipeline {} was interrupted", pipelineId);
            pipeline.setState(PipelineStateEnum.CANCELLED);
            pipeline.setEndTime(LocalDateTime.now());
            pipelineService.updatePipeline(pipeline);
        } catch (Exception e) {
            log.error("Error during pipeline {} execution", pipelineId, e);
            pipeline.setState(PipelineStateEnum.FAILED);
            pipeline.setEndTime(LocalDateTime.now());
            pipelineService.updatePipeline(pipeline);
        } finally {
            pipelineControlService.unregisterPipeline(pipelineId);
        }
    }

    /**
     * Execute a single step with pause/skip/cancel support
     * 
     * @return true if should continue, false if cancelled
     */
    private boolean executeStep(UUID pipelineId, Pipeline pipeline,
            PipelineStageEnum startStage, PipelineStageEnum completeStage,
            Runnable stepAction) throws InterruptedException {

        // Check if should skip this step
        if (pipelineControlService.shouldSkipCurrentStep(pipelineId)) {
            log.info("Skipping step {} for pipeline {}", startStage, pipelineId);
            pipeline.setStage(completeStage);
            pipelineService.updatePipeline(pipeline);
            return true;
        }

        // Wait if paused
        if (!pipelineControlService.waitWhilePaused(pipelineId)) {
            log.info("Pipeline {} cancelled while paused", pipelineId);
            pipeline.setState(PipelineStateEnum.CANCELLED);
            pipeline.setEndTime(LocalDateTime.now());
            pipelineService.updatePipeline(pipeline);
            return false;
        }

        // Check if cancelled
        if (!pipelineControlService.shouldContinue(pipelineId)) {
            log.info("Pipeline {} cancelled before step {}", pipelineId, startStage);
            pipeline.setState(PipelineStateEnum.CANCELLED);
            pipeline.setEndTime(LocalDateTime.now());
            pipelineService.updatePipeline(pipeline);
            return false;
        }

        // Update stage and execute
        pipeline.setStage(startStage);
        pipelineService.updatePipeline(pipeline);

        stepAction.run();

        pipeline.setStage(completeStage);
        pipelineService.updatePipeline(pipeline);

        return true;
    }

    /**
     * Execute all steps in sequence (async)
     * 
     * @return The pipeline ID for tracking
     */
    @Async
    public void executeAllSteps() {
        executeSteps(EnumSet.of(
                PipelineStageEnum.API_CALLER_STARTED,
                PipelineStageEnum.SCRAPER_STARTED,
                PipelineStageEnum.PARSER_STARTED));
    }

    /**
     * Pause the currently running pipeline
     */
    public Pipeline pauseCurrentPipeline() {
        return pipelineControlService.getActivePipelineId()
                .map(pipelineControlService::pausePipeline)
                .orElseThrow(() -> new IllegalStateException("No active pipeline to pause"));
    }

    /**
     * Resume the paused pipeline
     */
    public Pipeline resumeCurrentPipeline() {
        return pipelineControlService.getActivePipelineId()
                .map(pipelineControlService::resumePipeline)
                .orElseThrow(() -> new IllegalStateException("No paused pipeline to resume"));
    }

    /**
     * Skip the current step of the active pipeline
     */
    public Pipeline skipCurrentStep() {
        return pipelineControlService.getActivePipelineId()
                .map(pipelineControlService::skipCurrentStep)
                .orElseThrow(() -> new IllegalStateException("No active pipeline"));
    }

    /**
     * Cancel the current pipeline
     */
    public Pipeline cancelCurrentPipeline() {
        return pipelineControlService.getActivePipelineId()
                .map(pipelineControlService::cancelPipeline)
                .orElseThrow(() -> new IllegalStateException("No active pipeline to cancel"));
    }

    /**
     * Check if there's an active pipeline
     */
    public boolean hasActivePipeline() {
        return pipelineControlService.hasActivePipeline();
    }
}
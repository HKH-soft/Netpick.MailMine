package ir.netpick.mailmine.scrape.service.orch;

import ir.netpick.mailmine.common.enums.PipelineStageEnum;
import ir.netpick.mailmine.common.enums.PipelineStateEnum;
import ir.netpick.mailmine.scrape.model.Pipeline;
import ir.netpick.mailmine.scrape.service.base.PipelineService;
import ir.netpick.mailmine.scrape.service.mid.ApiCaller;
import ir.netpick.mailmine.scrape.service.mid.DataProcessor;
import ir.netpick.mailmine.scrape.service.mid.Scraper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScrapePipeline {

    private final ApiCaller apiCaller;
    private final Scraper scraper;
    private final DataProcessor dataProcessor;
    private final PipelineService pipelineService;

    @Scheduled(cron = "0 0 */3 * * *")
    public void scheduledScrapeJob() {
        log.info("===== Starting scheduled scraping pipeline =====");
        Pipeline pipeline = new Pipeline(PipelineStageEnum.STARTED, PipelineStateEnum.RUNNING, LocalDateTime.now());
        pipeline = pipelineService.createPipeline(pipeline);
        try {

            apiCaller.callGoogleSearch();
            pipeline.setStage(PipelineStageEnum.API_CALLER_COMPLETE);
            pipelineService.updatePipeline(pipeline);

            scraper.scrapePendingJobs();
            pipeline.setStage(PipelineStageEnum.SCRAPER_COMPLETE);
            pipelineService.updatePipeline(pipeline);

            dataProcessor.processUnparsedFiles();
            pipeline.setStage(PipelineStageEnum.PARSER_COMPLETE);
            pipeline.setState(PipelineStateEnum.COMPLETED);
            pipeline.setEndTime(LocalDateTime.now());
            pipelineService.updatePipeline(pipeline);
            log.info("===== Scraping pipeline completed successfully =====");
        } catch (Exception e) {
            log.error("Error during scheduled scraping job", e);
            pipeline.setState(PipelineStateEnum.FAILED);
            pipeline.setEndTime(LocalDateTime.now());
            pipelineService.updatePipeline(pipeline);
        }
    }
}

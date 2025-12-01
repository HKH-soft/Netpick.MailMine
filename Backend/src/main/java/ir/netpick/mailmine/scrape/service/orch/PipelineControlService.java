package ir.netpick.mailmine.scrape.service.orch;

import ir.netpick.mailmine.common.enums.PipelineStateEnum;
import ir.netpick.mailmine.scrape.model.Pipeline;
import ir.netpick.mailmine.scrape.service.base.PipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controls pipeline execution - pause, resume, skip, cancel operations.
 * Tracks active pipelines and provides thread-safe state management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineControlService {

    private final PipelineService pipelineService;

    // Track active pipeline states in memory for fast access
    private final Map<UUID, PipelineStateEnum> activePipelineStates = new ConcurrentHashMap<>();

    /**
     * Register a pipeline as active
     */
    public void registerPipeline(UUID pipelineId) {
        activePipelineStates.put(pipelineId, PipelineStateEnum.RUNNING);
        log.info("Pipeline {} registered as active", pipelineId);
    }

    /**
     * Unregister a pipeline (when finished)
     */
    public void unregisterPipeline(UUID pipelineId) {
        activePipelineStates.remove(pipelineId);
        log.info("Pipeline {} unregistered", pipelineId);
    }

    /**
     * Check if a pipeline should continue running
     */
    public boolean shouldContinue(UUID pipelineId) {
        PipelineStateEnum state = activePipelineStates.get(pipelineId);
        return state == PipelineStateEnum.RUNNING || state == PipelineStateEnum.SKIPPING;
    }

    /**
     * Check if current step should be skipped
     */
    public boolean shouldSkipCurrentStep(UUID pipelineId) {
        PipelineStateEnum state = activePipelineStates.get(pipelineId);
        if (state == PipelineStateEnum.SKIPPING) {
            // Reset to running after skip is acknowledged
            activePipelineStates.put(pipelineId, PipelineStateEnum.RUNNING);
            return true;
        }
        return false;
    }

    /**
     * Check if pipeline is paused
     */
    public boolean isPaused(UUID pipelineId) {
        return activePipelineStates.get(pipelineId) == PipelineStateEnum.PAUSED;
    }

    /**
     * Wait while pipeline is paused (blocking call with periodic checks)
     * Returns true if resumed, false if cancelled
     */
    public boolean waitWhilePaused(UUID pipelineId) throws InterruptedException {
        while (isPaused(pipelineId)) {
            log.debug("Pipeline {} is paused, waiting...", pipelineId);
            Thread.sleep(1000); // Check every second

            // Check if cancelled while paused
            if (activePipelineStates.get(pipelineId) == PipelineStateEnum.CANCELLED) {
                return false;
            }
        }
        return shouldContinue(pipelineId);
    }

    /**
     * Get current state of a pipeline
     */
    public PipelineStateEnum getState(UUID pipelineId) {
        return activePipelineStates.getOrDefault(pipelineId, PipelineStateEnum.PENDING);
    }

    /**
     * Pause a running pipeline
     */
    public Pipeline pausePipeline(UUID pipelineId) {
        Pipeline pipeline = pipelineService.getPipeline(pipelineId);

        if (pipeline.getState() != PipelineStateEnum.RUNNING) {
            throw new IllegalStateException("Cannot pause pipeline in state: " + pipeline.getState());
        }

        activePipelineStates.put(pipelineId, PipelineStateEnum.PAUSED);
        pipeline.setState(PipelineStateEnum.PAUSED);
        pipelineService.updatePipeline(pipeline);

        log.info("Pipeline {} paused at stage {}", pipelineId, pipeline.getStage());
        return pipeline;
    }

    /**
     * Resume a paused pipeline
     */
    public Pipeline resumePipeline(UUID pipelineId) {
        Pipeline pipeline = pipelineService.getPipeline(pipelineId);

        if (pipeline.getState() != PipelineStateEnum.PAUSED) {
            throw new IllegalStateException("Cannot resume pipeline in state: " + pipeline.getState());
        }

        activePipelineStates.put(pipelineId, PipelineStateEnum.RUNNING);
        pipeline.setState(PipelineStateEnum.RUNNING);
        pipelineService.updatePipeline(pipeline);

        log.info("Pipeline {} resumed at stage {}", pipelineId, pipeline.getStage());
        return pipeline;
    }

    /**
     * Skip the current step and continue to next
     */
    public Pipeline skipCurrentStep(UUID pipelineId) {
        Pipeline pipeline = pipelineService.getPipeline(pipelineId);

        if (!pipeline.getState().isActive()) {
            throw new IllegalStateException("Cannot skip step in pipeline state: " + pipeline.getState());
        }

        activePipelineStates.put(pipelineId, PipelineStateEnum.SKIPPING);
        log.info("Pipeline {} will skip current stage: {}", pipelineId, pipeline.getStage());
        return pipeline;
    }

    /**
     * Cancel a pipeline completely
     */
    public Pipeline cancelPipeline(UUID pipelineId) {
        Pipeline pipeline = pipelineService.getPipeline(pipelineId);

        if (pipeline.getState().isFinished()) {
            throw new IllegalStateException("Cannot cancel finished pipeline");
        }

        activePipelineStates.put(pipelineId, PipelineStateEnum.CANCELLED);
        pipeline.setState(PipelineStateEnum.CANCELLED);
        pipeline.setEndTime(java.time.LocalDateTime.now());
        pipelineService.updatePipeline(pipeline);

        log.info("Pipeline {} cancelled at stage {}", pipelineId, pipeline.getStage());
        return pipeline;
    }

    /**
     * Get the currently active pipeline (if any)
     */
    public Optional<UUID> getActivePipelineId() {
        return activePipelineStates.entrySet().stream()
                .filter(e -> e.getValue().isActive())
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Check if there's an active pipeline running
     */
    public boolean hasActivePipeline() {
        return activePipelineStates.values().stream()
                .anyMatch(PipelineStateEnum::isActive);
    }

    /**
     * Check pipeline state and handle pause/cancel during long-running operations.
     * Call this periodically within loops.
     * 
     * @return true if should continue, false if should stop (cancelled or skipping)
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean checkAndWait() throws InterruptedException {
        Optional<UUID> activePipeline = getActivePipelineId();
        if (activePipeline.isEmpty()) {
            return true; // No active pipeline, continue
        }

        UUID pipelineId = activePipeline.get();
        PipelineStateEnum state = activePipelineStates.get(pipelineId);

        // If cancelled or skipping, stop current operation
        if (state == PipelineStateEnum.CANCELLED || state == PipelineStateEnum.SKIPPING) {
            log.info("Pipeline {} is {}, stopping current operation", pipelineId, state);
            return false;
        }

        // If paused, wait until resumed or cancelled
        if (state == PipelineStateEnum.PAUSED) {
            log.info("Pipeline {} is paused, waiting...", pipelineId);
            return waitWhilePaused(pipelineId);
        }

        return true;
    }

    /**
     * Non-blocking check if should stop current operation
     */
    public boolean shouldStop() {
        Optional<UUID> activePipeline = getActivePipelineId();
        if (activePipeline.isEmpty()) {
            return false;
        }

        PipelineStateEnum state = activePipelineStates.get(activePipeline.get());
        return state == PipelineStateEnum.CANCELLED ||
                state == PipelineStateEnum.SKIPPING ||
                state == PipelineStateEnum.PAUSED;
    }
}

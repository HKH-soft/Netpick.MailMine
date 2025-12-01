package ir.netpick.mailmine.scrape.service.base;

import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.constants.GeneralConstants;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.scrape.model.Pipeline;
import ir.netpick.mailmine.scrape.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PipelineService {

    private final PipelineRepository pipelineRepository;

    public boolean isEmpty() {
        return pipelineRepository.count() == 0;
    }

    public PageDTO<Pipeline> allPipelines(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Pipeline> page = pipelineRepository.findByDeletedFalse(pageable);
        return new PageDTO<>(
                page.getContent(),
                page.getTotalPages(),
                pageNumber);
    }

    public PageDTO<Pipeline> deletedPipelines(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Pipeline> page = pipelineRepository.findByDeletedTrue(pageable);
        return new PageDTO<>(
                page.getContent(),
                page.getTotalPages(),
                pageNumber);
    }

    public PageDTO<Pipeline> allPipelinesIncludingDeleted(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Pipeline> page = pipelineRepository.findAll(pageable);
        return new PageDTO<>(
                page.getContent(),
                page.getTotalPages(),
                pageNumber);
    }

    public Pipeline getPipeline(UUID pipelineId) {
        return pipelineRepository.findById(pipelineId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Pipeline with id %s was not found".formatted(pipelineId)));
    }

    public Pipeline getPipelineIncludingDeleted(UUID pipelineId) {
        return pipelineRepository.findById(pipelineId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Pipeline with id %s was not found".formatted(pipelineId)));
    }

    public Pipeline createPipeline(Pipeline pipeline) {
        return pipelineRepository.save(pipeline);
    }

    public void updatePipeline(Pipeline pipeline) {
        pipelineRepository.save(pipeline);
    }

    public void updatePipeline(UUID pipelineId, Pipeline pipeline) {
        if (!pipelineRepository.existsById(pipelineId)) {
            throw new ResourceNotFoundException("Pipeline with id %s was not found".formatted(pipelineId));
        }
        pipeline.setId(pipelineId);
        pipelineRepository.save(pipeline);
    }

    public void softDeletePipeline(UUID pipelineId) {
        pipelineRepository.softDelete(pipelineId);
    }

    public void restorePipeline(UUID pipelineId) {
        pipelineRepository.restore(pipelineId);
    }

    public void deletePipeline(Pipeline pipeline) {
        pipelineRepository.delete(pipeline);
    }

}
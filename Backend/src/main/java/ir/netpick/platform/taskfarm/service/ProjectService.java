package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.taskfarm.model.Project;
import ir.netpick.platform.taskfarm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public PageDTO<Project> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Project> page = projectRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Project> getByOwner(UUID ownerId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Project> page = projectRepository.findByOwnerIdAndDeletedFalse(ownerId, pageable);
        return PageDTOMapper.map(page);
    }

    public Project getById(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project with id [%s] was not found".formatted(projectId)));
    }

    public Project create(Project project) {
        project.setId(null);
        return projectRepository.save(project);
    }

    public Project update(UUID projectId, Project project) {
        Project existing = getById(projectId);
        project.setId(projectId);
        project.setCreatedAt(existing.getCreatedAt());
        return projectRepository.save(project);
    }

    public void delete(UUID projectId) {
        projectRepository.softDelete(projectId);
    }

    public void restore(UUID projectId) {
        projectRepository.restore(projectId);
    }

    public Map<String, Object> getStats(UUID projectId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("taskCount", projectRepository.countTasksByProjectId(projectId));
        return stats;
    }
}
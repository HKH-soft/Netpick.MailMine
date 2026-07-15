package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.taskfarm.model.Task;
import ir.netpick.platform.taskfarm.model.TaskStatus;
import ir.netpick.platform.taskfarm.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public PageDTO<Task> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Task> page = taskRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Task> getByStatus(TaskStatus status, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Task> page = taskRepository.findByStatusAndDeletedFalse(status, pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Task> getByAssignee(UUID assigneeId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Task> page = taskRepository.findByAssigneeIdAndDeletedFalse(assigneeId, pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Task> getByProject(UUID projectId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("order").ascending());
        Page<Task> page = taskRepository.findByProjectIdAndDeletedFalse(projectId, pageable);
        return PageDTOMapper.map(page);
    }

    public Task getById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id [%s] was not found".formatted(taskId)));
    }

    public Task create(Task task) {
        task.setId(null);
        return taskRepository.save(task);
    }

    public Task update(UUID taskId, Task task) {
        Task existing = getById(taskId);
        task.setId(taskId);
        task.setCreatedAt(existing.getCreatedAt());
        return taskRepository.save(task);
    }

    public void delete(UUID taskId) {
        taskRepository.softDelete(taskId);
    }

    public void restore(UUID taskId) {
        taskRepository.restore(taskId);
    }

    public void reorder(UUID taskId, Integer newOrder) {
        Task task = getById(taskId);
        task.setOrder(newOrder);
        taskRepository.save(task);
    }
}
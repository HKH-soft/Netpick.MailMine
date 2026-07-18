package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.taskfarm.model.Label;
import ir.netpick.platform.taskfarm.model.Task;
import ir.netpick.platform.taskfarm.model.TaskLabel;
import ir.netpick.platform.taskfarm.repository.LabelRepository;
import ir.netpick.platform.taskfarm.repository.TaskLabelRepository;
import ir.netpick.platform.taskfarm.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final TaskLabelRepository taskLabelRepository;
    private final TaskRepository taskRepository;

    public PageDTO<Label> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Label> page = labelRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public List<Label> getByProject(UUID projectId) {
        return labelRepository.findByProjectIdAndDeletedFalse(projectId);
    }

    public Label getById(UUID labelId) {
        return labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id [%s] not found".formatted(labelId)));
    }

    public Label create(Label label) {
        label.setId(null);
        return labelRepository.save(label);
    }

    public Label update(UUID labelId, Label label) {
        Label existing = getById(labelId);
        label.setId(labelId);
        label.setCreatedAt(existing.getCreatedAt());
        return labelRepository.save(label);
    }

    public void delete(UUID labelId) {
        labelRepository.deleteById(labelId);
    }

    public void addLabelToTask(UUID taskId, UUID labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        Label label = getById(labelId);
        TaskLabel taskLabel = new TaskLabel(taskId, labelId);
        taskLabel.setTask(task);
        taskLabel.setLabel(label);
        taskLabelRepository.save(taskLabel);
    }

    public void removeLabelFromTask(UUID taskId, UUID labelId) {
        taskLabelRepository.deleteByTaskIdAndLabelId(taskId, labelId);
    }

    public List<Label> getLabelsForTask(UUID taskId) {
        List<TaskLabel> taskLabels = taskLabelRepository.findByTaskId(taskId);
        return taskLabels.stream()
                .map(TaskLabel::getLabel)
                .toList();
    }
}
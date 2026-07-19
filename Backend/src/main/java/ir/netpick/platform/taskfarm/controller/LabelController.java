package ir.netpick.platform.taskfarm.controller;

import ir.netpick.platform.taskfarm.dto.LabelDTO;
import ir.netpick.platform.taskfarm.model.Label;
import ir.netpick.platform.taskfarm.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/taskfarm/labels")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class LabelController {

    private final LabelService labelService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(labelService.getAll(page));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(labelService.getByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(labelService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LabelDTO request) {
        Label label = new Label();
        label.setName(request.name());
        label.setColor(request.color());
        label.setProjectId(request.projectId());
        label.setCreatedById(request.createdById());
        return ResponseEntity.ok(labelService.create(label));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody LabelDTO request) {
        Label label = new Label();
        label.setName(request.name());
        label.setColor(request.color());
        label.setProjectId(request.projectId());
        label.setCreatedById(request.createdById());
        return ResponseEntity.ok(labelService.update(id, label));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        labelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{taskId}/labels/{labelId}")
    public ResponseEntity<?> addLabelToTask(@PathVariable UUID taskId, @PathVariable UUID labelId) {
        labelService.addLabelToTask(taskId, labelId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tasks/{taskId}/labels/{labelId}")
    public ResponseEntity<?> removeLabelFromTask(@PathVariable UUID taskId, @PathVariable UUID labelId) {
        labelService.removeLabelFromTask(taskId, labelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getLabelsForTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(labelService.getLabelsForTask(taskId));
    }
}
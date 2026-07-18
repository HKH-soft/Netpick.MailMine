package ir.netpick.platform.taskfarm.controller;

import ir.netpick.platform.taskfarm.dto.TaskDTO;
import ir.netpick.platform.taskfarm.model.Task;
import ir.netpick.platform.taskfarm.model.TaskStatus;
import ir.netpick.platform.taskfarm.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * TaskFarm - Tasks and Projects Controller
 */
@RestController
@RequestMapping("/api/v1/taskfarm/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<?> getAllTasks(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(taskService.getAll(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDTO request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.valueOf(request.status()));
        task.setPriority(ir.netpick.platform.taskfarm.model.TaskPriority.valueOf(request.priority()));
        task.setProjectId(request.projectId());
        task.setAssigneeId(request.assigneeId());
        task.setCreatorId(request.creatorId());
        task.setDueDate(request.dueDate());
        task.setOrder(request.order());
        return ResponseEntity.ok(taskService.create(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable UUID id, @RequestBody TaskDTO request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.valueOf(request.status()));
        task.setPriority(ir.netpick.platform.taskfarm.model.TaskPriority.valueOf(request.priority()));
        task.setProjectId(request.projectId());
        task.setAssigneeId(request.assigneeId());
        task.setCreatorId(request.creatorId());
        task.setDueDate(request.dueDate());
        task.setOrder(request.order());
        return ResponseEntity.ok(taskService.update(id, task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreTask(@PathVariable UUID id) {
        taskService.restore(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable String status, @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(taskService.getByStatus(TaskStatus.valueOf(status), page));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getByProject(@PathVariable UUID projectId, @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(taskService.getByProject(projectId, page));
    }

    @PutMapping("/{id}/reorder")
    public ResponseEntity<?> reorderTask(@PathVariable UUID id, @RequestBody ReorderRequest request) {
        taskService.move(id, TaskStatus.valueOf(request.status()), request.order());
        return ResponseEntity.ok().build();
    }

    public record ReorderRequest(String status, Integer order) {}
}
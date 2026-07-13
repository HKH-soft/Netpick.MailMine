package ir.netpick.platform.taskfarm.controller;

import ir.netpick.platform.taskfarm.dto.TaskDTO;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * TaskFarm - Tasks and Projects Controller
 */
@RestController
@RequestMapping("/api/v1/taskfarm/tasks")
@RequiredArgsConstructor
public class TaskController {

    // TODO: Add TaskService dependency

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks(@AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(null);
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable UUID id, @RequestBody TaskDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
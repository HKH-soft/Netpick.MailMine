package ir.netpick.platform.taskfarm.controller;

import ir.netpick.platform.taskfarm.dto.ProjectDTO;
import ir.netpick.platform.taskfarm.model.Project;
import ir.netpick.platform.taskfarm.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * TaskFarm - Projects Controller
 */
@RestController
@RequestMapping("/api/v1/taskfarm/projects")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<?> getAllProjects(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(projectService.getAll(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setOwnerId(request.ownerId());
        project.setStatus(Project.ProjectStatus.valueOf(request.status()));
        return ResponseEntity.ok(projectService.create(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable UUID id, @RequestBody ProjectDTO request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setOwnerId(request.ownerId());
        project.setStatus(Project.ProjectStatus.valueOf(request.status()));
        return ResponseEntity.ok(projectService.update(id, project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreProject(@PathVariable UUID id) {
        projectService.restore(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getStats(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getStats(id));
    }
}
package ir.netpick.platform.taskfarm.controller;

import ir.netpick.platform.taskfarm.dto.CommentDTO;
import ir.netpick.platform.taskfarm.model.Comment;
import ir.netpick.platform.taskfarm.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/taskfarm/comments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getByTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(commentService.getByTask(taskId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(commentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CommentDTO request) {
        Comment comment = new Comment();
        comment.setTaskId(request.taskId());
        comment.setAuthorId(request.authorId());
        comment.setContent(request.content());
        comment.setParentId(request.parentId());
        return ResponseEntity.ok(commentService.create(comment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CommentDTO request) {
        Comment comment = new Comment();
        comment.setTaskId(request.taskId());
        comment.setAuthorId(request.authorId());
        comment.setContent(request.content());
        comment.setParentId(request.parentId());
        return ResponseEntity.ok(commentService.update(id, comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
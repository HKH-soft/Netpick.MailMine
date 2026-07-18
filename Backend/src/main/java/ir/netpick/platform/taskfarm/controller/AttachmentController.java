package ir.netpick.platform.taskfarm.controller;

import ir.netpick.platform.taskfarm.dto.AttachmentDTO;
import ir.netpick.platform.taskfarm.model.Attachment;
import ir.netpick.platform.taskfarm.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/taskfarm/attachments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getByTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(attachmentService.getByTask(taskId));
    }

    @PostMapping("/upload/{taskId}")
    public ResponseEntity<?> upload(@PathVariable UUID taskId,
                                  @RequestParam("file") MultipartFile file,
                                  @RequestParam("userId") UUID userId) throws IOException {
        Attachment attachment = attachmentService.upload(taskId, file, userId);
        return ResponseEntity.ok(toDTO(attachment));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable UUID id) throws IOException {
        Attachment attachment = attachmentService.getById(id);
        Path path = Path.of(attachment.getFilePath());
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) throws IOException {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private AttachmentDTO toDTO(Attachment attachment) {
        return new AttachmentDTO(
            attachment.getId(),
            attachment.getTaskId(),
            attachment.getFilename(),
            attachment.getOriginalFilename(),
            attachment.getFilePath(),
            attachment.getFileSize(),
            attachment.getContentType(),
            attachment.getUploadedById(),
            attachment.getCreatedAt(),
            attachment.getUpdatedAt()
        );
    }
}
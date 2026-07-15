package ir.netpick.platform.filefarm.controller;

import ir.netpick.platform.filefarm.dto.FileDTO;
import ir.netpick.platform.filefarm.model.FileEntity;
import ir.netpick.platform.filefarm.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * FileFarm - Documents and File Management Controller
 */
@RestController
@RequestMapping("/api/v1/filefarm/files")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<?> getAllFiles(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(fileService.getAll(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFile(@PathVariable UUID id) {
        return ResponseEntity.ok(fileService.getById(id));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false) UUID ownerId) throws IOException {
        return ResponseEntity.ok(fileService.upload(file, folderId, ownerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFile(@PathVariable UUID id, @RequestBody FileDTO request) {
        FileEntity file = new FileEntity();
        file.setFileName(request.fileName());
        file.setOriginalFileName(request.originalFileName());
        file.setMimeType(request.mimeType());
        file.setFileSize(request.fileSize());
        file.setFilePath(request.filePath());
        file.setFolderId(request.folderId());
        file.setOwnerId(request.ownerId());
        return ResponseEntity.ok(fileService.update(id, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable UUID id) {
        fileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreFile(@PathVariable UUID id) {
        fileService.restore(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<?> getByFolder(@PathVariable UUID folderId, @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(fileService.getByFolder(folderId, page));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByName(@RequestParam String name, @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(fileService.searchByName(name, page));
    }
}
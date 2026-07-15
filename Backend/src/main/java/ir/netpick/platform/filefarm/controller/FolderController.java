package ir.netpick.platform.filefarm.controller;

import ir.netpick.platform.filefarm.dto.FolderDTO;
import ir.netpick.platform.filefarm.model.Folder;
import ir.netpick.platform.filefarm.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * FileFarm - Folders Controller
 */
@RestController
@RequestMapping("/api/v1/filefarm/folders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    public ResponseEntity<?> getAllFolders(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(folderService.getAll(page));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getByOwner(@PathVariable UUID ownerId) {
        return ResponseEntity.ok(folderService.getByOwner(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFolder(@PathVariable UUID id) {
        return ResponseEntity.ok(folderService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> createFolder(@RequestBody FolderDTO request) {
        Folder folder = new Folder();
        folder.setName(request.name());
        folder.setParentId(request.parentId());
        folder.setOwnerId(request.ownerId());
        folder.setPath(request.path());
        return ResponseEntity.ok(folderService.create(folder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFolder(@PathVariable UUID id, @RequestBody FolderDTO request) {
        Folder folder = new Folder();
        folder.setName(request.name());
        folder.setParentId(request.parentId());
        folder.setOwnerId(request.ownerId());
        folder.setPath(request.path());
        return ResponseEntity.ok(folderService.update(id, folder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFolder(@PathVariable UUID id) {
        folderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreFolder(@PathVariable UUID id) {
        folderService.restore(id);
        return ResponseEntity.ok().build();
    }
}
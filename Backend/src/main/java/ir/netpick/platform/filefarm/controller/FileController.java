package ir.netpick.platform.filefarm.controller;

import ir.netpick.platform.filefarm.dto.FileDTO;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * FileFarm - Documents and File Management Controller
 */
@RestController
@RequestMapping("/api/v1/filefarm/files")
@RequiredArgsConstructor
public class FileController {

    // TODO: Add FileService dependency

    @GetMapping
    public ResponseEntity<List<FileDTO>> getAllFiles(@AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDTO> getFile(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(null);
    }

    @PostMapping
    public ResponseEntity<FileDTO> uploadFile(@RequestBody FileDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileDTO> updateFile(@PathVariable UUID id, @RequestBody FileDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
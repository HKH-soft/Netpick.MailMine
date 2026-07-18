package ir.netpick.platform.taskfarm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttachmentDTO(
    UUID id,
    UUID taskId,
    String filename,
    String originalFilename,
    String filePath,
    Long fileSize,
    String contentType,
    UUID uploadedById,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
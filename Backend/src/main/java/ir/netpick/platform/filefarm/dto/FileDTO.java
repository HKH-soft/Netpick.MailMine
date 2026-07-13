package ir.netpick.platform.filefarm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FileFarm - Documents and File Management DTO
 */
public record FileDTO(
    UUID id,
    String fileName,
    String originalFileName,
    String mimeType,
    Long fileSize,
    String filePath,
    UUID folderId,
    UUID ownerId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
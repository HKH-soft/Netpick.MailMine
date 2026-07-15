package ir.netpick.platform.filefarm.dto;

import java.util.UUID;

/**
 * FileFarm - Folder DTO
 */
public record FolderDTO(
    UUID id,
    String name,
    UUID parentId,
    UUID ownerId,
    String path
) {}
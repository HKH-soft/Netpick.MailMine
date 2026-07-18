package ir.netpick.platform.taskfarm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LabelDTO(
    UUID id,
    String name,
    String color,
    UUID projectId,
    UUID createdById,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
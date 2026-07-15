package ir.netpick.platform.taskfarm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TaskFarm - Tasks and Projects DTO
 */
public record TaskDTO(
    UUID id,
    String title,
    String description,
    String status,
    String priority,
    UUID projectId,
    UUID assigneeId,
    UUID creatorId,
    LocalDateTime dueDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime completedAt,
    Integer order
) {}
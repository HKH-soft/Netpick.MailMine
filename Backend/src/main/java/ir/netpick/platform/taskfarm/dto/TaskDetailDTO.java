package ir.netpick.platform.taskfarm.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TaskDetailDTO(
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
    Integer order,
    List<LabelDTO> labels,
    List<CommentDTO> comments
) {}
package ir.netpick.platform.taskfarm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDTO(
    UUID id,
    UUID taskId,
    UUID authorId,
    String content,
    UUID parentId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
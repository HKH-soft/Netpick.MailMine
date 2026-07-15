package ir.netpick.platform.taskfarm.dto;

import java.util.UUID;

/**
 * TaskFarm - Project DTO
 */
public record ProjectDTO(
    UUID id,
    String name,
    String description,
    UUID ownerId,
    String status
) {}
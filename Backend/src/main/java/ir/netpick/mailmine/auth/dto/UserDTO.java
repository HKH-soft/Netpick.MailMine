package ir.netpick.mailmine.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import ir.netpick.mailmine.common.enums.RoleEnum;

public record UserDTO(
        UUID id,
        String email,
        String name,
        RoleEnum role,
        String preference,
        UUID profileImageId,
        LocalDateTime created_at,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt

) {

}

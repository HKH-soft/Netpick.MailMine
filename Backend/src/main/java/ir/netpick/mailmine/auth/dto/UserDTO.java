package ir.netpick.mailmine.auth.dto;

import ir.netpick.mailmine.common.enums.RoleEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String email,
        String name,
        RoleEnum role,
        Boolean isVerified,
        LocalDateTime created_at,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt

) {

}

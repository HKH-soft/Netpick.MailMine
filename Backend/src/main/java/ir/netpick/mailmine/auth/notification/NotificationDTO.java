package ir.netpick.mailmine.auth.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDTO(
        UUID id,
        String title,
        String message,
        NotificationType type,
        boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt) {

}

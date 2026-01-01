package ir.netpick.mailmine.auth.notification;

import java.util.function.Function;

import org.springframework.stereotype.Service;

@Service
public class NotificationDTOMapper implements Function<Notification, NotificationDTO> {

    @Override
    public NotificationDTO apply(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt());
    }

}

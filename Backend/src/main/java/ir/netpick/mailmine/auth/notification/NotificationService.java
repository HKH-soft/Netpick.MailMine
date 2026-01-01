package ir.netpick.mailmine.auth.notification;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.service.UserService;
import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.common.utils.PageDTOMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDTOMapper notificationDTOMapper;
    private final NotificationStreamManager notificationStreamManager;
    private final UserService userService;

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new InsufficientAuthenticationException("Unauthorized");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
    }

    public PageDTO<NotificationDTO> allNotifications(UUID userId, Integer page) {

        Pageable pageable = PageRequest.of(page - 1, NotificationConstants.NOTIFICATION_LIST_SIZE,
                Sort.by("createdAt").descending());

        Page<Notification> notifications = notificationRepository.findByUser_Id(userId, pageable);

        return PageDTOMapper.map(notifications, notificationDTOMapper);
    }

    public PageDTO<NotificationDTO> unreadNotifications(UUID userId, Integer page) {

        Pageable pageable = PageRequest.of(page - 1, NotificationConstants.NOTIFICATION_LIST_SIZE,
                Sort.by("createdAt").descending());

        Page<Notification> notifications = notificationRepository.findByUser_IdAndIsReadFalse(userId, pageable);

        return PageDTOMapper.map(notifications, notificationDTOMapper);
    }

    public void createNotification(UUID userId, String title, String message, NotificationType type) {
        User user = userService.getUserEntity(userId);
        Notification notification = notificationRepository.save(new Notification(user, title, message, type));

        notificationStreamManager.sendNotification(userId, notificationDTOMapper.apply(notification));
    }

    public SseEmitter subscribeUser(UUID userId) {
        return notificationStreamManager.register(userId);
    }

}

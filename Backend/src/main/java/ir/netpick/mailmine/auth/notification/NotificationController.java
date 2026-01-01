package ir.netpick.mailmine.auth.notification;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.common.PageDTO;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public PageDTO<NotificationDTO> userNotifications(@RequestParam(defaultValue = "1") Integer page,
            @AuthenticationPrincipal User user) {
        return notificationService.allNotifications(user.getId(), page);
    }

    @GetMapping("unread")
    public PageDTO<NotificationDTO> unreadUserNotifications(@RequestParam(defaultValue = "1") Integer page,
            @AuthenticationPrincipal User user) {
        return notificationService.unreadNotifications(user.getId(), page);
    }

    @PostMapping("mark-read")
    public void markAllRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user.getId());
    }

    @PostMapping("{id}/mark-read")
    public void markOneAsRead(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        notificationService.markAsRead(id, user.getId());
    }

}

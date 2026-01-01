package ir.netpick.mailmine.auth.notification;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ir.netpick.mailmine.auth.model.User;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationSSE {

    private final NotificationService notificationService;

    @GetMapping("stream")
    public SseEmitter subscribe(@AuthenticationPrincipal User user) {
        return notificationService.subscribeUser(user.getId());
    }

}

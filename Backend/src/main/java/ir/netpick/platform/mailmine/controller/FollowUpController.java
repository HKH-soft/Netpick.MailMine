package ir.netpick.platform.mailmine.controller;

import ir.netpick.platform.mailmine.service.FollowUpDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mailmine/follow-ups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Slf4j
public class FollowUpController {

    private final FollowUpDetectionService followUpDetectionService;

    @GetMapping("/dashboard")
    public ResponseEntity<List<Map<String, Object>>> getDashboard() {
        return ResponseEntity.ok(followUpDetectionService.getFollowUpDashboard());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamFollowUps() {
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        final String[] listenerIdRef = new String[1];

        listenerIdRef[0] = followUpDetectionService.registerListener(notification -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("follow-up")
                        .data(notification));
            } catch (Exception e) {
                log.error("Failed to send SSE event: {}", e.getMessage());
                followUpDetectionService.removeListener(listenerIdRef[0]);
                emitter.completeWithError(e);
            }
        });

        emitter.onCompletion(() -> followUpDetectionService.removeListener(listenerIdRef[0]));
        emitter.onTimeout(() -> followUpDetectionService.removeListener(listenerIdRef[0]));
        emitter.onError(e -> followUpDetectionService.removeListener(listenerIdRef[0]));

        return emitter;
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerDetection() {
        followUpDetectionService.detectUnansweredEmails();
        return ResponseEntity.ok("Follow-up detection triggered");
    }
}










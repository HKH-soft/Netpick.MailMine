package ir.netpick.mailmine.auth.notification;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationStreamManager {
    private final Map<UUID, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(UUID userId) {
        SseEmitter emitter = new SseEmitter(0l);

        emitters
                .computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet())
                .add(emitter);

        Runnable cleanUp = () -> removeEmitter(userId, emitter);

        emitter.onCompletion(cleanUp);
        emitter.onTimeout(cleanUp);
        emitter.onError(e -> cleanUp.run());

        return emitter;
    }

    public void sendNotification(UUID userId, NotificationDTO notification) {
        send(userId, SseEmitter.event()
                .name("notification")
                .data(notification));
    }

    public void send(UUID userId, Object event) {
        Set<SseEmitter> userEmitters = emitters.get(userId);

        if (userEmitters == null)
            return;

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(event);
            } catch (Exception e) {
                int emitterId = System.identityHashCode(emitter);
                log.warn(
                        "Failed to send SSE event to user {} (emitterId={}). Removing emitter to prevent resource leak.",
                        userId, emitterId, e);
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        Set<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null)
            return;

        userEmitters.remove(emitter);

        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}

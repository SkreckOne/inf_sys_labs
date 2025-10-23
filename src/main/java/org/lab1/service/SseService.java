package org.lab1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter) {
        this.emitters.add(emitter);
        log.info("New SSE client connected. Total clients: {}", emitters.size());
        emitter.onCompletion(() -> removeEmitter(emitter));
        emitter.onError(e -> {
            log.info("SSE emitter error notification for a disconnected client.");
            removeEmitter(emitter);
        });
    }

    private void removeEmitter(SseEmitter emitter) {
        this.emitters.remove(emitter);
        log.info("SSE client disconnected. Total clients: {}", emitters.size());
    }

    public void sendEventToAll(String eventName, Object data) {
        SseEmitter.SseEventBuilder event = SseEmitter.event().name(eventName).data(data);
        for (SseEmitter emitter : this.emitters) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                log.info("Failed to send to a disconnected client. Removing client. Message: {}", e.getMessage());
                removeEmitter(emitter);
            }
        }
    }
}
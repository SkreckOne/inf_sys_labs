package org.lab1.service;

import lombok.extern.slf4j.Slf4j;
import org.lab1.service.SseEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Планировщик для периодической отправки "heartbeat" сообщений
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SseService() {
        // Запускаем задачу, которая будет отправлять heartbeat каждые 20 секунд
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 20, 20, TimeUnit.SECONDS);
    }

    /**
     * Добавляет нового клиента (SseEmitter) в список для рассылки.
     * @param emitter объект SseEmitter, связанный с клиентом.
     */
    public void addEmitter(SseEmitter emitter) {
        String emitterId = emitter.toString(); // Уникальный идентификатор для логов
        this.emitters.add(emitter);
        log.info("[SSE] Client connected. ID: {}. Total clients: {}", emitterId, emitters.size());

        // Обработчик на случай, когда соединение завершается штатно
        emitter.onCompletion(() -> {
            log.info("[SSE] Client connection completed. ID: {}", emitterId);
            removeEmitter(emitter);
        });

        // Обработчик на случай ошибки
        emitter.onError(e -> {
            log.error("[SSE] Client connection error. ID: {}. Error: {}", emitterId, e.getMessage());
            removeEmitter(emitter);
        });

        // Обработчик на случай таймаута
        emitter.onTimeout(() -> {
            log.warn("[SSE] Client connection timed out. ID: {}", emitterId);
            removeEmitter(emitter);
        });
    }

    /**
     * Удаляет клиента из списка рассылки.
     * @param emitter объект SseEmitter для удаления.
     */
    private void removeEmitter(SseEmitter emitter) {
        if (this.emitters.remove(emitter)) {
            log.info("[SSE] Client removed. ID: {}. Total clients: {}", emitter.toString(), emitters.size());
        }
    }

    /**
     * Слушает внутреннее событие приложения, которое публикуется после успешного
     * завершения транзакции в базе данных.
     * @param event Событие, содержащее имя и данные для отправки клиенту.
     */
    @TransactionalEventListener
    public void handleSseEvent(SseEvent event) {
        log.info("[SSE] Transaction committed. Preparing to send event '{}' to {} clients.", event.getName(), emitters.size());
        sendEventToAll(event.getName(), event.getData());
    }

    /**
     * Отправляет событие всем подключенным клиентам.
     * @param eventName Имя события (например, "movie-created").
     * @param data      Данные для отправки.
     */
    public void sendEventToAll(String eventName, Object data) {
        if (emitters.isEmpty()) {
            log.warn("[SSE] No clients connected, event '{}' was not sent.", eventName);
            return;
        }

        SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().name(eventName).data(data);
        log.info("[SSE] Sending event '{}' to {} clients.", eventName, emitters.size());

        // Проходим по каждому клиенту и отправляем событие
        for (SseEmitter emitter : this.emitters) {
            String emitterId = emitter.toString();
            try {
                emitter.send(eventBuilder);
                log.info("[SSE] >>> Successfully sent event '{}' to client ID: {}", eventName, emitterId);
            } catch (IOException e) {
                log.warn("[SSE] !!! Failed to send event to client ID: {}. Removing client. Reason: {}", emitterId, e.getMessage());
                removeEmitter(emitter);
            } catch (Exception e) {
                log.error("[SSE] !!! An unexpected error occurred for client ID: {}. Removing client. Error: {}", emitterId, e.getMessage());
                removeEmitter(emitter);
            }
        }
    }

    /**
     * Отправляет "heartbeat" (комментарий) всем клиентам для поддержания соединения в активном состоянии.
     * SSE-комментарии (начинаются с ":") игнорируются клиентом, но предотвращают обрыв соединения по таймауту.
     */
    private void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        SseEmitter.SseEventBuilder heartbeatEvent = SseEmitter.event().comment("keep-alive");
        log.info("[SSE] Sending keep-alive heartbeat to {} clients.", emitters.size());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(heartbeatEvent);
            } catch (IOException e) {
                // Это нормальная ситуация, если клиент закрыл вкладку. Просто удаляем его из списка.
                removeEmitter(emitter);
            }
        }
    }
}
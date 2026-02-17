package com.stationTracker.controller;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SseEmittersController {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter add(SseEmitter emitter) {
        this.emitters.add(emitter);

        // Send an immediate empty event to "warm up" the connection
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected"));
        } catch (IOException e) {
            emitter.complete();
        }

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        return emitter;
    }

    public void send(Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                // Explicitly use the 'data' and a media type to ensure the browser
                // doesn't think the connection is interrupted.
                emitter.send(data);
            } catch (Exception e) {
                // If we catch an error, we MUST call complete() before removing
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }

    public boolean hasActiveClients() {
        return !emitters.isEmpty();
    }
}
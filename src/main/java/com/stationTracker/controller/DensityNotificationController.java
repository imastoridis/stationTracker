package com.stationTracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/density")
public class DensityNotificationController {

    private final SseEmitters emitters = new SseEmitters();

    @GetMapping("/stream")
    public SseEmitter stream() {
        return emitters.add(new SseEmitter(3600 * 1000L));
    }

    // Call this from your Consumer
    public void broadcast(String density) {
        emitters.send(density);
    }
}

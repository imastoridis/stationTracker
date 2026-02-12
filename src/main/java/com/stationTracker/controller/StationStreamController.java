package com.stationTracker.controller;

import com.stationTracker.dto.StationStatusUpdate;
import com.stationTracker.service.producer.SncfArrivalProducer;
import com.stationTracker.service.producer.SncfDepartureProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class StationStreamController {

    private final SseEmittersController emitters = new SseEmittersController();
    private final SncfArrivalProducer sncfArrivalProducer;
    private final SncfDepartureProducer sncfDepartureProducer;

    public StationStreamController(SncfArrivalProducer sncfArrivalProducer, SncfDepartureProducer sncfDepartureProducer) {
        this.sncfArrivalProducer = sncfArrivalProducer;
        this.sncfDepartureProducer = sncfDepartureProducer;
    }

    /* Broadcast to front using the emitters */
    public void broadcast(StationStatusUpdate statusUpdate) {
        emitters.send(statusUpdate);
    }

    /* Creates a new SSE connection for 30 minutes*/
    @GetMapping("/stream")
    @SuppressWarnings("unused")
    public SseEmitter stream() {
        return emitters.add(new SseEmitter(1800 * 1000L));
    }

    /* Fetch the data manually*/
    @PostMapping("/trigger-fetch/arrivals")
    @SuppressWarnings("unused")
    public ResponseEntity<String> manualTriggerArrivals() {
        sncfArrivalProducer.produceData();
        return ResponseEntity.ok("Fetch arrivals triggered successfully");
    }

    @PostMapping("/trigger-fetch/departures")
    @SuppressWarnings("unused")
    public ResponseEntity<String> manualTriggerDepartures() {
        sncfDepartureProducer.produceData();
        return ResponseEntity.ok("Fetch departures triggered successfully");
    }
}

package com.stationTracker.controller;

import com.stationTracker.dto.StationStatusUpdate;
import com.stationTracker.service.producer.ArrivalProducer;
import com.stationTracker.service.producer.DepartureProducer;
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
    private final ArrivalProducer arrivalProducer;
    private final DepartureProducer departureProducer;

    public StationStreamController(ArrivalProducer arrivalProducer, DepartureProducer departureProducer) {
        this.arrivalProducer = arrivalProducer;
        this.departureProducer = departureProducer;
    }

    /* Broadcast to front using the emitters */
    public void broadcast(StationStatusUpdate statusUpdate) {
        emitters.send(statusUpdate);
    }

    /* Creates a new SSE connection for 30 minutes*/
    @GetMapping("/stream")
    @SuppressWarnings("unused")
    public SseEmitter stream() {
        //Create the emitter when someone connects
        boolean isFirstClient = !emitters.hasActiveClients();
        SseEmitter emitter = emitters.add(new SseEmitter(1800 * 1000L));

        if (isFirstClient) {
            // Trigger an immediate fetch so the first user doesn't see a blank map
            arrivalProducer.produceDataArrivals();
            departureProducer.produceDataDepartures();
        }

        return emitter;
    }

    /* Fetch the data manually*/
    @PostMapping("/trigger-fetch/arrivals")
    @SuppressWarnings("unused")
    public ResponseEntity<String> manualTriggerArrivals() {
        arrivalProducer.produceDataArrivals();
        return ResponseEntity.ok("Fetch arrivals triggered successfully");
    }

    @PostMapping("/trigger-fetch/departures")
    @SuppressWarnings("unused")
    public ResponseEntity<String> manualTriggerDepartures() {
        departureProducer.produceDataDepartures();
        return ResponseEntity.ok("Fetch departures triggered successfully");
    }
}

package com.stationTracker.service.producer;

import com.stationTracker.controller.SseEmittersController;
import com.stationTracker.dto.departure.TrainDepartureBatchEvent;
import com.stationTracker.dto.departure.TrainDepartureEvent;
import com.stationTracker.mapper.departure.DeparturesMapper;
import org.springframework.http.HttpEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("unused")
public class DepartureProducer extends Producer<TrainDepartureEvent, TrainDepartureBatchEvent> {

    private final KafkaTemplate<String, TrainDepartureEvent> kafkaTemplate;
    private final DeparturesMapper departuresMapper = new DeparturesMapper();
    private static final String SNCF_API_URL_DEPARTURES = SNCF_API_URL + "/departures";
    private final SseEmittersController sseEmittersController;

    public DepartureProducer(KafkaTemplate<String, TrainDepartureEvent> kafkaTemplate, SseEmittersController sseEmittersController) {
        this.kafkaTemplate = kafkaTemplate;
        this.sseEmittersController = sseEmittersController;
    }

    /**
     * Main scheduled task that orchestrates the fetch and produce cycle
     * Fetches the arrivals and coordinates of each train
     */
    @Scheduled(fixedRate = 60000) // Poll every 60 seconds
    public void produceDataDepartures() {
        List<TrainDepartureEvent> events = new ArrayList<>();

        try {
            // Check if there are any active clients
            if (!sseEmittersController.hasActiveClients()) {
                return;
            }
            HttpEntity<String> entity = setBasicAuth();

            // Fetches data
            try {
                Map<String, Object> rawData = fetchRawApiData(SNCF_API_URL_DEPARTURES + "?datetime=" + now, entity);
                events = departuresMapper.mapResponseToDto(rawData);

                for (TrainDepartureEvent event : events) {
                    fetchCoordinates(entity, event);
                }
            } catch (Exception apiException) {
                if (apiException.getMessage().contains("404")) {
                    System.out.println("No departures found for the current time. Sending empty batch.");
                } else {
                    throw apiException; // Re-throw if it's a real error (like 500 or Auth)
                }
            }

            // Send to kafka
            kafkaTemplate.send("train-departures", "SNCF_BATCH_DEPARTURES", new TrainDepartureBatchEvent(events));
        } catch (Exception e) {
            System.err.println("Failed to produce departure data: " + e.getMessage());
        }
    }


}
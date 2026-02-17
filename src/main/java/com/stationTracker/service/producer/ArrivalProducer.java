package com.stationTracker.service.producer;

import com.stationTracker.controller.SseEmittersController;
import com.stationTracker.controller.StationStreamController;
import com.stationTracker.dto.arrival.TrainArrivalBatchEvent;
import com.stationTracker.dto.arrival.TrainArrivalEvent;
import com.stationTracker.mapper.arrival.ArrivalsMapper;
import org.springframework.http.HttpEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("unused")
public class ArrivalProducer extends Producer<TrainArrivalEvent, TrainArrivalBatchEvent> {

    private final KafkaTemplate<String, TrainArrivalEvent> kafkaTemplate;
    private final ArrivalsMapper arrivalsMapper = new ArrivalsMapper();
    private static final String SNCF_API_URL_ARRIVALS = SNCF_API_URL + "/arrivals";
    private final SseEmittersController sseEmittersController;

    public ArrivalProducer(KafkaTemplate<String, TrainArrivalEvent> kafkaTemplate, SseEmittersController sseEmittersController) {
        this.kafkaTemplate = kafkaTemplate;
        this.sseEmittersController = sseEmittersController;
    }

    /**
     * Main scheduled task that orchestrates the fetch and produce cycle
     * Fetches the arrivals and coordinates of each train
     */
    @Scheduled(fixedRate = 60000) // Poll every 60 seconds
    public void produceDataArrivals() {
        try {
            // Check if there are any active clients
            if (!sseEmittersController.hasActiveClients()) {
                return;
            }

            HttpEntity<String> entity = setBasicAuth();

            // Fetches data
            Map<String, Object> rawData = fetchRawApiData(SNCF_API_URL_ARRIVALS + "?datetime=" + now, entity);
            List<TrainArrivalEvent> events = arrivalsMapper.mapResponseToDto(rawData);

            // Fetches coordinates
            for (TrainArrivalEvent event : events) {
                fetchCoordinates(entity, event);
            }

            // Send to kafka
            kafkaTemplate.send("train-arrivals", "SNCF_BATCH_ARRIVALS", new TrainArrivalBatchEvent(events));
        } catch (Exception e) {
            System.err.println("Failed to produce arrivals data: " + e.getMessage());
        }
    }
}
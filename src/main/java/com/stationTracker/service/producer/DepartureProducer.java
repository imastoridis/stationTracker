package com.stationTracker.service.producer;

import com.stationTracker.dto.departure.TrainDepartureBatchEvent;
import com.stationTracker.dto.departure.TrainDepartureEvent;
import com.stationTracker.mapper.departure.DeparturesMapper;
import org.springframework.http.HttpEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("unused")
public class DepartureProducer extends Producer<TrainDepartureEvent, TrainDepartureBatchEvent> {

    private final KafkaTemplate<String, TrainDepartureEvent> kafkaTemplate;
    private final DeparturesMapper departuresMapper = new DeparturesMapper();
    private static final String SNCF_API_URL_DEPARTURES = SNCF_API_URL + "/departures";

    public DepartureProducer(KafkaTemplate<String, TrainDepartureEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Main scheduled task that orchestrates the fetch and produce cycle
     * Fetches the arrivals and coordinates of each train
     */
    //@Scheduled(fixedRate = 60000) // Poll every 60 seconds
    public void produceDataDepartures() {
        try {
            HttpEntity<String> entity = setBasicAuth();

            // Fetches data
            Map<String, Object> rawData = fetchRawApiData(SNCF_API_URL_DEPARTURES + "?datetime=" + now, entity);
            List<TrainDepartureEvent> events = departuresMapper.mapResponseToDto(rawData);

            // Fetches coordinates
            for (TrainDepartureEvent event : events) {
                fetchCoordinates(entity, event);
            }

            // Send to kafka
            kafkaTemplate.send("train-departures", "SNCF_BATCH_DEPARTURES", new TrainDepartureBatchEvent(events));
        } catch (Exception e) {
            System.err.println("Failed to produce departure data: " + e.getMessage());
        }
    }


}
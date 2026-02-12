package com.stationTracker.service.producer;

import com.stationTracker.dto.departure.TrainDepartureBatchEvent;
import com.stationTracker.dto.departure.TrainDepartureEvent;
import com.stationTracker.mapper.DeparturesMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("unused")
@Slf4j
public class SncfDepartureProducer {

    private final KafkaTemplate<String, TrainDepartureEvent> kafkaTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final DeparturesMapper departuresMapper = new DeparturesMapper();
    @Value("${sncf.api.key}")
    private String apiKey;

    // URL for Lyon Part-Dieu departures
    private static final String SNCF_API_URL =
            "https://api.sncf.com/v1/coverage/sncf/stop_areas/stop_area:SNCF:87723197/departures";

    public SncfDepartureProducer(KafkaTemplate<String, TrainDepartureEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Main scheduled task that orchestrates the fetch and produce cycle
     * Fetches the departures
     */
    //@Scheduled(fixedRate = 60000) // Poll every 60 seconds
    public void produceData() {
        try {
            System.out.println("Polling SNCF for departure data...");

            //Auth
            HttpEntity<String> entity = setBasicAuth();
            // Api call
            List<TrainDepartureEvent> events = getApiEvents(entity);
            // Get coordinates of trains
        /*    for (TrainDepartureEvent event : events) {
                fetchAndSetCoordinates(event, entity);
            }*/
            // Send to kafka
            sendToKafka(events);
        } catch (Exception e) {
            System.err.println("Failed to produce departure data: " + e.getMessage());
        }
    }

    /**
     * Calls the vehicle_journeys endpoint to get the real-time position of the train
     */
    private void fetchAndSetCoordinates(TrainDepartureEvent event, HttpEntity<String> entity) {
        try {
            // The vehicle_journey_id is typically what you have in event.getTrainId()
            String journeyUrl = "https://api.sncf.com/v1/coverage/sncf/vehicle_journeys/" + event.getTrainId();

            ResponseEntity<Map> response = restTemplate.exchange(
                    journeyUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() != null) {
                // JSON: vehicle_journeys[0] -> physical_vehicle -> position
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> journeys = (List<Map<String, Object>>) body.get("vehicle_journeys");

                if (journeys != null && !journeys.isEmpty()) {
                    Map<String, Object> physicalVehicle = (Map<String, Object>) journeys.get(0).get("physical_vehicle");
                    if (physicalVehicle != null) {
                        Map<String, Object> position = (Map<String, Object>) physicalVehicle.get("position");
                        if (position != null) {
                            event.setLatitude((Double) position.get("lat"));
                            event.setLongitude((Double) position.get("lon"));
                            log.info("Coords found for train {}: {}, {}", event.getTrainId(), event.getLatitude(), event.getLongitude());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch coordinates for train {}: {}", event.getTrainId(), e.getMessage());
        }
    }

    /* Auth to API*/
    private HttpEntity<String> setBasicAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        return new HttpEntity<>(headers);
    }

    /* Api call */
    private List<TrainDepartureEvent> getApiEvents(HttpEntity<String> entity) {

        // Add the datetime parameter to the URL to avoid duplicate on fetching
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
        String urlWithTimestamp = SNCF_API_URL + "?datetime=" + now;

        ParameterizedTypeReference<Map<String, Object>> typeRef =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                urlWithTimestamp,
                HttpMethod.GET,
                entity,
                typeRef
        );

        // Print headers to see quota info
        response.getHeaders().forEach((key, value) -> {
            if (key.toLowerCase().contains("ratelimit")) {
                System.out.println(key + " : " + value);
            }
        });

        // Map to dto
        return departuresMapper.mapResponseToDto(response.getBody());
    }

    /* Send to kafka*/
    private void sendToKafka(List<TrainDepartureEvent> events) {
        TrainDepartureBatchEvent batch = new TrainDepartureBatchEvent(events);
        kafkaTemplate.send("train-departures", "SNCF_BATCH_DEPARTURES", batch);
    }
}
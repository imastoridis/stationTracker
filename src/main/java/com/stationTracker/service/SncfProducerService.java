package com.stationTracker.service;

import com.stationTracker.dto.TrainArrivalEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SncfProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sncf.api.key}")
    private String apiKey;

    private static final String SNCF_API_URL =
            "https://api.sncf.com/v1/coverage/sncf/stop_areas/stop_area:SNCF:87723166/arrivals";

    public SncfProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 60000) // Poll every 1 minute
    public void pollStationData() {
        try {
            // Note: In a real app, you'd add the Authorization header with your API Key
            // For now, we simulate the fetch and produce to Kafka
            System.out.println("Polling SNCF for Lyon Part-Dieu...");

            // Logic to parse the SNCF JSON response would go here
            TrainArrivalEvent mockEvent = new TrainArrivalEvent();
            mockEvent.setTrainId("TGV-6601");
            mockEvent.setOrigin("Paris Gare de Lyon");
            mockEvent.setArrivalTime("2026-02-05T14:30:00");
            mockEvent.setDelay(5);

            // Push to Kafka Topic
            kafkaTemplate.send("train-arrivals", "lyon-part-dieu", mockEvent);

        } catch (Exception e) {
            System.err.println("Failed to fetch data: " + e.getMessage());
        }
    }
}
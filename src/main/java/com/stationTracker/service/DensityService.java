package com.stationTracker.service;

import com.stationTracker.model.TrainArrival;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DensityService {

    @KafkaListener(topics = "train-arrivals", groupId = "density-calculator")
    public void processArrival(TrainArrival event) {
        // 1. Calculate density for the next 15-minute slot
        // 2. Determine if it's HIGH (> 5 trains) or LOW (< 2 trains)
        // 3. Update the database via Hibernate
        // 4. Send a 'DensityUpdate' to the frontend via WebSocket or Server-Sent Events (SSE)
    }
}

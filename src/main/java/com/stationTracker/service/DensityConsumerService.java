package com.stationTracker.service;

import com.stationTracker.controller.DensityNotificationController;
import com.stationTracker.dto.TrainArrivalEvent;
import com.stationTracker.model.TrainArrival;
import com.stationTracker.repository.TrainArrivalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class DensityConsumerService {

    private final TrainArrivalRepository repository;
    private final DensityNotificationController uiController;

    @KafkaListener(topics = "train-arrivals", groupId = "station-tracker-group")
    public void consumeArrival(TrainArrivalEvent event) {
        log.info("Received arrival: {}", event.getTrainId());

        // 1. Map DTO to Entity and Save
        TrainArrival arrival = new TrainArrival();
        arrival.setTrainNumber(event.getTrainId());
        arrival.setOrigin(event.getOrigin());
        arrival.setScheduledArrivalTime(LocalDateTime.parse(event.getArrivalTime()));
        arrival.setDelayInMinutes(event.getDelay());
        repository.save(arrival);

        // 2. Calculate Density Score (Trains in the next 30 minutes)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(30);

        long trainCount = repository.countArrivalsInWindow(now, windowEnd);
        String densityLevel = determineDensityLevel(trainCount);

        log.info("Current Density for Lyon Part-Dieu: {} ({} trains expected)", densityLevel, trainCount);

        // Push to the map via SSE
        uiController.broadcast(densityLevel);
    }

    private String determineDensityLevel(long count) {
        if (count > 8) return "CRITICAL";
        if (count > 5) return "HIGH";
        if (count > 2) return "MEDIUM";
        return "LOW";
    }
}
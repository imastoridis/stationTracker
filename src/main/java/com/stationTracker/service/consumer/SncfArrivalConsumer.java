package com.stationTracker.service.consumer;

import com.stationTracker.controller.StationStreamController;
import com.stationTracker.dto.StationStatusUpdate;
import com.stationTracker.dto.arrival.TrainArrivalBatchEvent;
import com.stationTracker.dto.arrival.TrainArrivalEvent;
import com.stationTracker.model.TrainArrival;
import com.stationTracker.repository.TrainArrivalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SncfArrivalConsumer {

    private final TrainArrivalRepository repository;
    private final StationStreamController uiController;

    @KafkaListener(topics = "train-arrivals", groupId = "station-tracker-arrivals")
    @Transactional
    public void consumeArrival(TrainArrivalBatchEvent batch) {
        //  Map batch DTO to Entity and Save to db
        saveTrains(batch);

        // Get the trains arriving in the next 30 minutes
        List<TrainArrival> arrivalEntities = getTrains();

        // Find density level and upcoming trains
        StationStatusUpdate statusUpdate = getStatusUpdate(arrivalEntities);

        // Broadcast to UI via SSE
        uiController.broadcast(statusUpdate);
    }

    /* Saves the trains, only if new or delay has changed */
    private void saveTrains(TrainArrivalBatchEvent batch) {
        log.info("Received batch of {} arrivals", batch.getArrivals().size());

        List<TrainArrival> trainsToSave = new ArrayList<>();

        for (TrainArrivalEvent event : batch.getArrivals()) {
            //Check if the train already exists in the DB
            Optional<TrainArrival> existingTrainOpt = repository.findByTrainIdAndTrainNumber(event.getTrainId(), event.getTrainNumber());

            if (existingTrainOpt.isPresent()) {
                TrainArrival existingTrain = existingTrainOpt.get();

                // Only update if the delay has actually changed
                if (existingTrain.getDelayInMinutes() != event.getDelay()) {
                    log.info("Updating delay for train {}: {} -> {}",
                            event.getTrainId(), existingTrain.getDelayInMinutes(), event.getDelay());

                    existingTrain.setDelayInMinutes(event.getDelay());
                    trainsToSave.add(existingTrain);
                }
            } else {
                // If new train, create and add it
                log.info("Saving new train: {}", event.getTrainId());
                TrainArrival newArrival = new TrainArrival();
                newArrival.setTrainId(event.getTrainId());
                newArrival.setTrainNumber(event.getTrainNumber());
                newArrival.setOrigin(event.getOrigin());
                newArrival.setScheduledArrivalTime(LocalDateTime.parse(event.getArrivalTime()));
                newArrival.setDelayInMinutes(event.getDelay());
                trainsToSave.add(newArrival);
            }
        }

        if (!trainsToSave.isEmpty()) {
            repository.saveAllAndFlush(trainsToSave);
        } else {
            log.info("No changes detected in train delays. Skipping database save.");
        }
    }

    /* Calculate density of station */
    private List<TrainArrival> getTrains() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now);
        LocalDateTime windowEnd = now.plusMinutes(30);
        return repository.getTrains(now, windowEnd);
    }

    /* Find density level and upcoming trains*/
    private StationStatusUpdate getStatusUpdate(List<TrainArrival> arrivalEntities) {
        // Count the number of trains in the next 30 minutes
        long trainCount = arrivalEntities.size();
        String densityLevel = determineDensityLevel(trainCount);

        log.info("Current Density for Lyon Part-Dieu: {} ({} trains expected)", densityLevel, trainCount);

        return StationStatusUpdate.builder()
                .densityLevel(densityLevel)
                .trainCount(trainCount)
                .upcomingTrains(arrivalEntities.stream().map(this::mapToDto).toList())
                .build();
    }

    private String determineDensityLevel(long count) {
        if (count > 8) return "CRITICAL";
        if (count > 5) return "HIGH";
        if (count > 2) return "MEDIUM";
        return "LOW";
    }

    /* Mapper */
    private TrainArrivalEvent mapToDto(TrainArrival entity) {
        TrainArrivalEvent dto = new TrainArrivalEvent();
        dto.setTrainId(entity.getTrainId());
        dto.setTrainNumber(entity.getTrainNumber());
        dto.setOrigin(entity.getOrigin());
        dto.setArrivalTime(entity.getScheduledArrivalTime().toString());
        dto.setDelay(entity.getDelayInMinutes());
        return dto;
    }




}
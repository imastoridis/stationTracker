package com.stationTracker.service.consumer;

import com.stationTracker.controller.StationStreamController;
import com.stationTracker.dto.StationStatusUpdate;
import com.stationTracker.dto.departure.TrainDepartureBatchEvent;
import com.stationTracker.dto.departure.TrainDepartureEvent;
import com.stationTracker.model.TrainDeparture;
import com.stationTracker.repository.TrainDepartureRepository;
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
public class SncfDepartureConsumer {

    private final TrainDepartureRepository repository;
    private final StationStreamController uiController;

    @KafkaListener(topics = "train-departures", groupId = "station-tracker-departures")
    @Transactional
    public void consumeDeparture(TrainDepartureBatchEvent batch) {
        //  Map batch DTO to Entity and Save to db
        saveTrains(batch);

        // Get the trains arriving in the next 30 minutes
        List<TrainDeparture> DepartureEntities = getTrains();

        // Find density level and upcoming trains
        StationStatusUpdate statusUpdate = getStatusUpdate(DepartureEntities);

        // Broadcast to UI via SSE
        uiController.broadcast(statusUpdate);
    }

    /* Saves the trains, only if new or delay has changed */
    private void saveTrains(TrainDepartureBatchEvent batch) {
        log.info("Received batch of {} Departures", batch.getDepartures().size());

        List<TrainDeparture> trainsToSave = new ArrayList<>();

        for (TrainDepartureEvent event : batch.getDepartures()) {
            //Check if the train already exists in the DB
            Optional<TrainDeparture> existingTrainOpt = repository.findByTrainIdAndTrainNumber(event.getTrainId(), event.getTrainNumber());

            if (existingTrainOpt.isPresent()) {
                TrainDeparture existingTrain = existingTrainOpt.get();

                // Only update if the delay has actually changed
                if (existingTrain.getDelayInMinutes() != event.getDelay() ) {
                    log.info("Updating delay for train {}: {} -> {}",
                            event.getTrainId(), existingTrain.getDelayInMinutes(), event.getDelay());

                    existingTrain.setDelayInMinutes(event.getDelay());
                    trainsToSave.add(existingTrain);
                }
            } else {
                // If new train, create and add it
                log.info("Saving new train: {}", event.getTrainId());
                TrainDeparture newDeparture = new TrainDeparture();
                newDeparture.setTrainId(event.getTrainId());
                newDeparture.setTrainNumber(event.getTrainNumber());
                newDeparture.setDestination(event.getDestination());
                newDeparture.setScheduledDepartureTime(LocalDateTime.parse(event.getDepartureTime()));
                newDeparture.setDelayInMinutes(event.getDelay());
                trainsToSave.add(newDeparture);
            }
        }

        if (!trainsToSave.isEmpty()) {
            repository.saveAllAndFlush(trainsToSave);
        } else {
            log.info("No changes detected in train delays. Skipping database save.");
        }
    }

    /* Calculate density of station */
    private List<TrainDeparture> getTrains() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now);
        LocalDateTime windowEnd = now.plusMinutes(30);
        return repository.getTrains(now, windowEnd);
    }

    /* Find density level and upcoming trains*/
    private StationStatusUpdate getStatusUpdate(List<TrainDeparture> DepartureEntities) {
        // Count the number of trains in the next 30 minutes
        long trainCount = DepartureEntities.size();
        String densityLevel = determineDensityLevel(trainCount);

        log.info("Current Density for for departing trains Lyon Part-Dieu: {} ({} trains expected)", densityLevel, trainCount);

        return StationStatusUpdate.builder()
                .densityLevel(densityLevel)
                .trainCount(trainCount)
                .departingTrains(DepartureEntities.stream().map(this::mapToDto).toList())
                .build();
    }

    private String determineDensityLevel(long count) {
        if (count > 8) return "CRITICAL";
        if (count > 5) return "HIGH";
        if (count > 2) return "MEDIUM";
        return "LOW";
    }

    /* Mapper */
    private TrainDepartureEvent mapToDto(TrainDeparture entity) {
        TrainDepartureEvent dto = new TrainDepartureEvent();
        dto.setTrainId(entity.getTrainId());
        dto.setTrainNumber(entity.getTrainNumber());
        dto.setDestination(entity.getDestination());
        dto.setDepartureTime(entity.getScheduledDepartureTime().toString());
        dto.setDelay(entity.getDelayInMinutes());
        dto.setPlatform(entity.getPlatform());
        return dto;
    }




}
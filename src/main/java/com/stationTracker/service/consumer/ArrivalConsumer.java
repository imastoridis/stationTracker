package com.stationTracker.service.consumer;

import com.stationTracker.controller.StationStreamController;
import com.stationTracker.dto.arrival.TrainArrivalBatchEvent;
import com.stationTracker.dto.arrival.TrainArrivalEvent;
import com.stationTracker.mapper.arrival.ArrivalsEntityMapper;
import com.stationTracker.model.TrainArrival;
import com.stationTracker.repository.TrainArrivalRepository;
import com.stationTracker.service.StationStateService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("unused")
public class ArrivalConsumer extends Consumer<TrainArrival, TrainArrivalEvent> {

    private final TrainArrivalRepository repository;

    public ArrivalConsumer(
            TrainArrivalRepository repository,
            StationStreamController uiController,
            ArrivalsEntityMapper arrivalsEntityMapper,
            StationStateService stateService) {
        super(uiController, stateService);
        this.repository = repository;
    }

    /** Kafka listener for arrival events*/
    @KafkaListener(topics = "train-arrivals", groupId = "station-tracker-arrivals")
    @Transactional
    public void consumeArrival(TrainArrivalBatchEvent batch) {
        executeSynchronized(() -> {
            List<TrainArrival> entities = synchronizeDatabase(batch.getArrivals());
            repository.saveAll(entities);

            logBatchMetrics("ARRIVAL", batch.getArrivals().size());
        });
    }

    /** Finds if the train already exists in the arrival table*/
    @Override
    protected Optional<TrainArrival> findExisting(TrainArrivalEvent event) {
        return repository.findByTrainIdAndTrainNumber(event.getTrainId(), event.getTrainNumber());
    }

    /** Creates a new arrival entity*/
    @Override
    protected TrainArrival createNewEntity(TrainArrivalEvent event) {
        TrainArrival newArrival = new TrainArrival();
        newArrival.setTrainId(event.getTrainId());
        newArrival.setTrainNumber(event.getTrainNumber());
        newArrival.setOrigin(event.getOrigin());
        newArrival.setScheduledArrivalTime(LocalDateTime.parse(event.getArrivalTime()));
        updateEntity(newArrival, event); // Set shared dynamic fields
        return newArrival;
    }

    /** Updates an entity */
    @Override
    protected void updateEntity(TrainArrival entity, TrainArrivalEvent event) {
        entity.setDelayInMinutes(event.getDelay());
        entity.setLatitude(event.getLatitude());
        entity.setLongitude(event.getLongitude());

        logPositionUpdate(event.getTrainId(), event.getLatitude(), event.getLongitude());
    }
}

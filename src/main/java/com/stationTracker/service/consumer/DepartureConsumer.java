package com.stationTracker.service.consumer;

import com.stationTracker.controller.StationStreamController;
import com.stationTracker.dto.departure.TrainDepartureBatchEvent;
import com.stationTracker.dto.departure.TrainDepartureEvent;
import com.stationTracker.model.TrainDeparture;
import com.stationTracker.repository.TrainDepartureRepository;
import com.stationTracker.service.StationStateService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.stationTracker.mapper.departure.DeparturesEntityMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("unused")
public class DepartureConsumer extends Consumer<TrainDeparture, TrainDepartureEvent> {

    private final TrainDepartureRepository repository;

    public DepartureConsumer(
            TrainDepartureRepository repository,
            StationStreamController uiController,
            DeparturesEntityMapper departuresEntityMapper,
            StationStateService stationStateService
    ) {
        super(uiController, stationStateService);
        this.repository = repository;
    }

    /** Kafka listener for departure events*/
    @KafkaListener(topics = "train-departures", groupId = "station-tracker-departures")
    @Transactional
    public void consumeDeparture(TrainDepartureBatchEvent batch) {
        executeSynchronized(() -> {
            List<TrainDeparture> entities = synchronizeDatabase(batch.getDepartures());
            repository.saveAll(entities);

            logBatchMetrics("DEPARTURE", batch.getDepartures().size());
        });
    }

    /** Finds if the train already exists in the arrival table*/
    @Override
    protected Optional<TrainDeparture> findExisting(TrainDepartureEvent event) {
        return repository.findByTrainIdAndTrainNumber(event.getTrainId(), event.getTrainNumber());
    }

    /** Creates a new departure entity*/
    @Override
    protected TrainDeparture createNewEntity(TrainDepartureEvent event) {
        TrainDeparture newDeparture = new TrainDeparture();
        newDeparture.setTrainId(event.getTrainId());
        newDeparture.setTrainNumber(event.getTrainNumber());
        newDeparture.setDestination(event.getDestination());
        newDeparture.setScheduledDepartureTime(LocalDateTime.parse(event.getDepartureTime()));
        updateEntity(newDeparture, event); // Set shared dynamic fields
        return newDeparture;
    }

    /** Updates an entity */
    @Override
    protected void updateEntity(TrainDeparture entity, TrainDepartureEvent event) {
        entity.setDelayInMinutes(event.getDelay());
        entity.setLatitude(event.getLatitude());
        entity.setLongitude(event.getLongitude());
    }
}
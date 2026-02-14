package com.stationTracker.service.consumer;

import com.stationTracker.controller.StationStreamController;
import com.stationTracker.service.StationStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Consumer<T, E> {

    protected final StationStreamController uiController;
    protected static final Logger log = LoggerFactory.getLogger(Consumer.class);
    protected final StationStateService stateService;

    protected Consumer(StationStreamController uiController, StationStateService stateService) {
        this.uiController = uiController;
        this.stateService = stateService;
    }

    // Abstract method that subclasses must implement to define how to find an existing train
    protected abstract Optional<T> findExisting(E event);

    // Abstract method to map the Event DTO to a new Entity
    protected abstract T createNewEntity(E event);

    // Abstract method to update an existing Entity with new data (delay, coords)
    protected abstract void updateEntity(T entity, E event);

    /**
     * Executes kafka consumers for arrivals and departures
     * Waits for both consumers to finish before sending the status update
     */
    protected void executeSynchronized(Runnable ingestionTask) {
        stateService.signalTaskStart();
        try {
            ingestionTask.run();
        } finally {
            stateService.signalTaskEnd();
        }
    }

    /**
     * Updates if present or creates otherwise
     */
    protected List<T> synchronizeDatabase(List<E> events) {
        List<T> toSave = new ArrayList<>();
        for (E event : events) {
            findExisting(event).ifPresentOrElse(
                    existing -> {
                        updateEntity(existing, event);
                        toSave.add(existing);
                    },
                    () -> toSave.add(createNewEntity(event))
            );
        }
        return toSave;
    }

    /**
     * Logs
     */
    protected void logPositionUpdate(String trainId, double lat, double lon) {
        if (lat != 0 && lon != 0) {
            log.debug("Position tracked for {}: [{}, {}]", trainId, lat, lon);
        }
    }

    // Generic metrics logging
    protected void logBatchMetrics(String type, int size) {
        log.info("{} batch processed: {} items", type, size);
    }

    /**
     * Density level
     */
    public String determineDensityLevel(long count) {
        if (count > 8) return "CRITICAL";
        if (count > 5) return "HIGH";
        if (count > 2) return "MEDIUM";
        return "LOW";
    }

}

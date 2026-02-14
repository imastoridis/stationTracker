package com.stationTracker.service;

import com.stationTracker.controller.StationStreamController;
import com.stationTracker.dto.StationStatusUpdate;
import com.stationTracker.mapper.arrival.ArrivalsEntityMapper;
import com.stationTracker.mapper.departure.DeparturesEntityMapper;
import com.stationTracker.model.TrainArrival;
import com.stationTracker.model.TrainDeparture;
import com.stationTracker.repository.TrainArrivalRepository;
import com.stationTracker.repository.TrainDepartureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationStateService {
    private final TrainArrivalRepository arrivalRepo;
    private final TrainDepartureRepository departureRepo;
    private final ArrivalsEntityMapper arrivalMapper;
    private final DeparturesEntityMapper departureMapper;
    private final StationStreamController uiController;
    protected static final int LOOK_AHEAD_MINUTES = 30;

    // Thread-safe counter to track active Kafka consumers
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    public void signalTaskStart() {
        activeTasks.incrementAndGet();
    }

    public void signalTaskEnd() {
        // If this was the last active task, perform the broadcast
        if (activeTasks.decrementAndGet() == 0) {
            performBroadcast();
        }
    }

    private void performBroadcast() {
        log.info("Deterministic broadcast triggered: All Kafka tasks complete.");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(LOOK_AHEAD_MINUTES);

        List<TrainArrival> arrivals = arrivalRepo.getTrains(now, windowEnd);
        List<TrainDeparture> departures = departureRepo.getTrains(now, windowEnd);

        StationStatusUpdate update = StationStatusUpdate.builder()
                .trainCount(arrivals.size() + departures.size())
                .densityLevel(determineDensity(arrivals.size() + departures.size()))
                .upcomingTrains(arrivals.stream().map(arrivalMapper::mapToDto).toList())
                .departingTrains(departures.stream().map(departureMapper::mapToDto).toList())
                .build();

        uiController.broadcast(update);
    }

    private String determineDensity(long count) {
        return (count > 8) ? "CRITICAL" : (count > 5) ? "HIGH" : "LOW";
    }
}
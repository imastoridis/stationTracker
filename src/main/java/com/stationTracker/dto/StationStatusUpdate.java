package com.stationTracker.dto;

import com.stationTracker.dto.arrival.TrainArrivalEvent;
import com.stationTracker.dto.departure.TrainDepartureEvent;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StationStatusUpdate {
    private String densityLevel;
    private long trainCount;
    private List<TrainArrivalEvent> upcomingTrains;
    private List<TrainDepartureEvent> departingTrains;
}
package com.stationTracker.dto.departure;

import com.stationTracker.dto.TrainEvent;
import lombok.Data;

@Data
public class TrainDepartureEvent implements TrainEvent {
    private String id;
    private String trainId;
    private String trainNumber;
    private Double latitude;
    private Double longitude;
    private int delay; // In minutes
    private String platform;
    private String journeyId;

    private String destination;
    private String departureTime;

}
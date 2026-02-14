package com.stationTracker.dto.arrival;

import com.stationTracker.dto.TrainEvent;
import lombok.Data;

@Data
public class TrainArrivalEvent implements TrainEvent {
    private String id;
    private String trainId;
    private String trainNumber;
    private Double latitude;
    private Double longitude;
    private int delay; // In minutes
    private String platform;
    private String journeyId;

    private String origin;
    private String arrivalTime;

}


package com.stationTracker.dto.arrival;

import lombok.Data;

@Data
public class TrainArrivalEvent {
    private String id;
    private String trainId;
    private String trainNumber;
    private Double latitude;
    private Double longitude;
    private String origin;
    private String arrivalTime;
    private int delay; // In minutes
    private String platform;
}
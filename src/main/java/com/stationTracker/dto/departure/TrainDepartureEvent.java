package com.stationTracker.dto.departure;

import lombok.Data;

@Data
public class TrainDepartureEvent {
    private String id;
    private String trainId;
    private String trainNumber;
    private Double latitude;
    private Double longitude;
    private String destination;
    private String departureTime;
    private int delay; // In minutes
    private String platform;
}
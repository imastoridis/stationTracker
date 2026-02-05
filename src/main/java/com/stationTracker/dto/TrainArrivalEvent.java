package com.stationTracker.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrainArrivalEvent {
    private String trainId;
    private String origin;
    private String arrivalTime; // ISO-8601
    private int delay; // In minutes
    private String platform;
}
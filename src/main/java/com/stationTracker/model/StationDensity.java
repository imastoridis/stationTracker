package com.stationTracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class StationDensity {
    private String stationName; // Lyon Part-Dieu
    private long trainCount;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private String densityLevel; // LOW, MEDIUM, HIGH
}
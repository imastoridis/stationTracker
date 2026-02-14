package com.stationTracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TrainArrival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trainId;

    private String trainNumber;

    private String origin;

    private String platform;

    private LocalDateTime scheduledArrivalTime;

    private int delayInMinutes;

    private Double latitude;

    private Double longitude;
}
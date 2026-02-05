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

    private String trainNumber;
    private String origin;
    private LocalDateTime scheduledArrivalTime;
    private int delayInMinutes;
    private String status; // ON_TIME, DELAYED, CANCELLED
}
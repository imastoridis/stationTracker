package com.stationTracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TrainDeparture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trainId;

    private String trainNumber;

    private String destination;

    private String platform;

    private LocalDateTime scheduledDepartureTime;

    private int delayInMinutes;
}
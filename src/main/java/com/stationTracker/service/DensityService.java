package com.stationTracker.service;

import com.stationTracker.dto.TrainArrivalEvent;
import com.stationTracker.model.TrainArrival;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DensityService {

    @KafkaListener(topics = "train-arrivals", groupId = "density-calculator")
    // Change TrainArrival to TrainArrivalEvent
    public void processArrival(TrainArrivalEvent event) {
        // Now Spring can deserialize the JSON into the DTO correctly
    }
}

package com.stationTracker.mapper.arrival;

import com.stationTracker.dto.arrival.TrainArrivalEvent;
import com.stationTracker.model.TrainArrival;
import org.springframework.stereotype.Component;

@Component
public class ArrivalsEntityMapper {

    /* Mapper */
    public TrainArrivalEvent mapToDto(TrainArrival entity) {
        TrainArrivalEvent dto = new TrainArrivalEvent();
        dto.setTrainId(entity.getTrainId());
        dto.setTrainNumber(entity.getTrainNumber());
        dto.setOrigin(entity.getOrigin());
        dto.setArrivalTime(entity.getScheduledArrivalTime().toString());
        dto.setDelay(entity.getDelayInMinutes());
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
        return dto;
    }
}

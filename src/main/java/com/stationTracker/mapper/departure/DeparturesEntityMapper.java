package com.stationTracker.mapper.departure;

import com.stationTracker.dto.departure.TrainDepartureEvent;
import com.stationTracker.model.TrainDeparture;
import org.springframework.stereotype.Component;

@Component
public class DeparturesEntityMapper {
    /* Mapper */
    public TrainDepartureEvent mapToDto(TrainDeparture entity) {
        TrainDepartureEvent dto = new TrainDepartureEvent();
        dto.setTrainId(entity.getTrainId());
        dto.setTrainNumber(entity.getTrainNumber());
        dto.setDestination(entity.getDestination());
        dto.setDepartureTime(entity.getScheduledDepartureTime().toString());
        dto.setDelay(entity.getDelayInMinutes());
        dto.setPlatform(entity.getPlatform());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        return dto;
    }
}

package com.stationTracker.dto.arrival;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainArrivalBatchEvent extends TrainArrivalEvent {
    private List<TrainArrivalEvent> arrivals;
}
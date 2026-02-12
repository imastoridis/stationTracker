package com.stationTracker.repository;

import com.stationTracker.model.TrainArrival;
import com.stationTracker.model.TrainDeparture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrainDepartureRepository extends JpaRepository<TrainDeparture, Long> {

    // Count arrivals for Lyon Part-Dieu within a specific time window
    @Query(
            "SELECT t FROM TrainDeparture t WHERE t.scheduledDepartureTime BETWEEN :start AND :end"
    )
    List<TrainDeparture>getTrains(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /* Get by train id and number*/
    Optional<TrainDeparture> findByTrainIdAndTrainNumber(String trainId, String trainNumber);
}


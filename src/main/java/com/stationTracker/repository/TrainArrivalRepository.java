package com.stationTracker.repository;

import com.stationTracker.model.TrainArrival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TrainArrivalRepository extends JpaRepository<TrainArrival, Long> {

    // Count arrivals for Lyon Part-Dieu within a specific time window
    @Query(
            "SELECT COUNT(t) FROM TrainArrival t WHERE t.scheduledArrivalTime BETWEEN :start AND :end"
    )
    long countArrivalsInWindow(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
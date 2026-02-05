package com.stationTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StationTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StationTrackerApplication.class, args);
    }

}

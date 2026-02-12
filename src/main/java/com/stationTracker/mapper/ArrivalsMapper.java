package com.stationTracker.mapper;

import com.stationTracker.dto.arrival.TrainArrivalEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ArrivalsMapper {

    private static final DateTimeFormatter SNCF_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getArrivalsList(Map<String, Object> body) {
        return (List<Map<String, Object>>) body.get("arrivals");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDisplayInfo(Map<String, Object> body) {
        return (Map<String, Object>) body.get("display_informations");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getRoute(Map<String, Object> body) {
        return (Map<String, Object>) body.get("route");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDirection(Map<String, Object> body) {
        return (Map<String, Object>) body.get("direction");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getStopDateTime(Map<String, Object> body) {
        return (Map<String, Object>) body.get("stop_date_time");
    }

    private Map<String, Object> getStopPoint(Map<String, Object> body) {
        return (Map<String, Object>) body.get("stop_point");
    }



    /*
     * DTO Mapping Logic
     * */
    public List<TrainArrivalEvent> mapResponseToDto(Map<String, Object> body) {
        List<TrainArrivalEvent> events = new ArrayList<>();
        if (body == null || !body.containsKey("arrivals")) return events;

        // Train arrivals
        List<Map<String, Object>> arrivals = getArrivalsList(body);

        for (Map<String, Object> arrival : arrivals) {
            try {
                TrainArrivalEvent event = new TrainArrivalEvent();

                // Get Data
                Map<String, Object> displayInfo = getDisplayInfo(arrival);
                Map<String, Object> route = getRoute(arrival);
                Map<String, Object> direction = getDirection(route);
                Map<String, Object> stopDateTime = getStopDateTime(arrival);

                // Train number
                event.setTrainNumber((String) displayInfo.get("headsign"));

                // Direction
                event.setOrigin((String) direction.get("name"));

                // Train Id
                event.setTrainId((String) route.get("id"));

                // Arrival Time and Calculate Delay
                String rawArrival = (String) stopDateTime.get("arrival_date_time");
                String rawBaseArrival = (String) stopDateTime.get("base_arrival_date_time");

                LocalDateTime actualTime = LocalDateTime.parse(rawArrival, SNCF_DATE_FORMATTER);
                LocalDateTime baseTime = LocalDateTime.parse(rawBaseArrival, SNCF_DATE_FORMATTER);

                long delayMinutes = Duration.between(baseTime, actualTime).toMinutes();

                event.setArrivalTime(baseTime.toString());
                event.setDelay((int) delayMinutes);

                // Platform
                Map<String, Object> stopPoint = getStopPoint(arrival);
                String platform = "TBD";

                if (stopPoint != null && stopPoint.get("platform_code") != null) {
                    platform = stopPoint.get("platform_code").toString();
                }
                event.setPlatform(platform);


                events.add(event);
            } catch (Exception e) {
                // Log and continue to the next item if one fails
                System.err.println("Error mapping individual arrival: " + e.getMessage());
            }
        }
        return events;
    }


}
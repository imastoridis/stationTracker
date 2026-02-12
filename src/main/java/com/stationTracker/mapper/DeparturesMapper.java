package com.stationTracker.mapper;

import com.stationTracker.dto.departure.TrainDepartureEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DeparturesMapper {

    private static final DateTimeFormatter SNCF_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getDeparturesList(Map<String, Object> body) {
        return (List<Map<String, Object>>) body.get("departures");
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

    /*
     * DTO Mapping Logic
     * */
    public List<TrainDepartureEvent> mapResponseToDto(Map<String, Object> body) {
        List<TrainDepartureEvent> events = new ArrayList<>();
        if (body == null || !body.containsKey("departures")) return events;

        // Train departures
        List<Map<String, Object>> departures = getDeparturesList(body);

        for (Map<String, Object> departure : departures) {
            try {
                TrainDepartureEvent event = new TrainDepartureEvent();

                // Get Data
                Map<String, Object> displayInfo = getDisplayInfo(departure);
                Map<String, Object> route = getRoute(departure);
                Map<String, Object> direction = getDirection(route);
                Map<String, Object> stopDateTime = getStopDateTime(departure);

                // Train number
                event.setTrainNumber((String) displayInfo.get("headsign"));

                // Direction
                event.setDestination((String) direction.get("name"));

                // Train Id
                event.setTrainId((String) route.get("id"));

                // Departure Time and Calculate Delay
                String rawDeparture = (String) stopDateTime.get("departure_date_time");
                String rawBaseDeparture = (String) stopDateTime.get("base_departure_date_time");

                LocalDateTime actualTime = LocalDateTime.parse(rawDeparture, SNCF_DATE_FORMATTER);
                LocalDateTime baseTime = LocalDateTime.parse(rawBaseDeparture, SNCF_DATE_FORMATTER);

                long delayMinutes = Duration.between(baseTime, actualTime).toMinutes();

                event.setDepartureTime(baseTime.toString());
                event.setDelay((int) delayMinutes);
                event.setPlatform("TBD");

                events.add(event);
            } catch (Exception e) {
                // Log and continue to the next item if one fails
                System.err.println("Error mapping individual departure: " + e.getMessage());
            }
        }
        return events;
    }


}
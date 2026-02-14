package com.stationTracker.service.producer;

import com.stationTracker.dto.TrainEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Producer<T extends TrainEvent, B> {

    protected final RestTemplate restTemplate = new RestTemplate();

    @Value("${sncf.api.key}")
    protected String apiKey;
    protected String journeyApiUrl = "https://api.sncf.com/v1/coverage/sncf/vehicle_journeys/";
    protected static final String SNCF_API_URL =
            "https://api.sncf.com/v1/coverage/sncf/stop_areas/stop_area:SNCF:87723197";
    protected String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));

    /**
     * Auth to API
     */
    protected HttpEntity<String> setBasicAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        return new HttpEntity<>(headers);
    }

    /**
     * Fetches raw data from SNCF API
     */
    protected Map<String, Object> fetchRawApiData(String url, HttpEntity<String> entity) {
        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, typeRef);
        return response.getBody();
    }

    /**
     * Fetches the coordinates of the train based on its journey ID
     */
    protected void fetchCoordinates(HttpEntity<String> entity, T event) {
        String journeyUrl = journeyApiUrl + event.getJourneyId();

        ResponseEntity<Map> response = restTemplate.exchange(journeyUrl, HttpMethod.GET, entity, Map.class);

        if (response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            String apiTimeStr = ((String) ((Map<String, Object>) body.get("context")).get("current_datetime")).substring(9);
            Map<String, Object> journey = ((List<Map<String, Object>>) body.get("vehicle_journeys")).getFirst();
            List<Map<String, Object>> stopTimes = (List<Map<String, Object>>) journey.get("stop_times");

            interpolate(
                    stopTimes,
                    apiTimeStr,
                    event::setCoordinates);
        }
    }

    /**
     * real-time position or estimate it via linear interpolation
     */
    protected void interpolate(List<Map<String, Object>> stopTimes, String apiTimeStr, CoordinateSetter setter) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
        LocalTime apiNow = LocalTime.parse(apiTimeStr, timeFormatter);

        for (int i = 0; i < stopTimes.size() - 1; i++) {
            Map<String, Object> stopA = stopTimes.get(i);
            Map<String, Object> stopB = stopTimes.get(i + 1);

            String depA = (String) stopA.get("departure_time");
            String arrB = (String) stopB.get("arrival_time");

            if (apiTimeStr.compareTo(depA) >= 0 && apiTimeStr.compareTo(arrB) <= 0) {
                Map<String, Object> coordA = (Map<String, Object>) ((Map<String, Object>) stopA.get("stop_point")).get("coord");
                Map<String, Object> coordB = (Map<String, Object>) ((Map<String, Object>) stopB.get("stop_point")).get("coord");

                double latA = Double.parseDouble(coordA.get("lat").toString());
                double lonA = Double.parseDouble(coordA.get("lon").toString());
                double latB = Double.parseDouble(coordB.get("lat").toString());
                double lonB = Double.parseDouble(coordB.get("lon").toString());

                long totalSecs = Duration.between(LocalTime.parse(depA, timeFormatter), LocalTime.parse(arrB, timeFormatter)).getSeconds();
                long elapsedSecs = Duration.between(LocalTime.parse(depA, timeFormatter), apiNow).getSeconds();

                double ratio = (totalSecs > 0) ? (double) elapsedSecs / totalSecs : 0.0;
                setter.setCoords(latA + (latB - latA) * ratio, lonA + (lonB - lonA) * ratio);
                return;
            }
        }

        // Fallback to the last stop
        Map<String, Object> destination = (Map<String, Object>) stopTimes.getLast().get("stop_point");
        Map<String, Object> coord = (Map<String, Object>) destination.get("coord");
        setter.setCoords(Double.parseDouble(coord.get("lat").toString()), Double.parseDouble(coord.get("lon").toString()));
    }

    @FunctionalInterface
    public interface CoordinateSetter {
        void setCoords(double lat, double lon);
    }
}

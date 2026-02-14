package com.stationTracker.dto;

/**
 * Interface to allow the generic Producer to interact with different Event types.
 */
public interface TrainEvent {
    String getJourneyId();
    String getTrainNumber();
    void setLatitude(Double lat);
    void setLongitude(Double lon);

    // Helper to match the CoordinateSetter functional interface
    default void setCoordinates(double lat, double lon) {
        setLatitude(lat);
        setLongitude(lon);
    }
}
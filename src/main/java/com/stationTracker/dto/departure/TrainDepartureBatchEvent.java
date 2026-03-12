package com.stationTracker.dto.departure;

import java.util.List;

public class TrainDepartureBatchEvent extends TrainDepartureEvent {
    private List<TrainDepartureEvent> departures;
    private long timestamp;

    public TrainDepartureBatchEvent(List<TrainDepartureEvent> departures) {
        this.departures = departures;
        this.timestamp = System.currentTimeMillis();
    }

    public TrainDepartureBatchEvent() {
    }

    public List<TrainDepartureEvent> getDepartures() {
        return this.departures;
    }

    public void setDepartures(List<TrainDepartureEvent> departures) {
        this.departures = departures;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TrainDepartureBatchEvent)) return false;
        final TrainDepartureBatchEvent other = (TrainDepartureBatchEvent) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$departures = this.getDepartures();
        final Object other$departures = other.getDepartures();
        if (this$departures == null ? other$departures != null : !this$departures.equals(other$departures))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TrainDepartureBatchEvent;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $departures = this.getDepartures();
        result = result * PRIME + ($departures == null ? 43 : $departures.hashCode());
        return result;
    }

    public String toString() {
        return "TrainDepartureBatchEvent(departures=" + this.getDepartures() + ")";
    }

    public long getTimestamp() {
        return this.timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
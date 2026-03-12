package com.stationTracker.dto.arrival;

import java.util.List;

public class TrainArrivalBatchEvent extends TrainArrivalEvent {
    private List<TrainArrivalEvent> arrivals;
    private long timestamp;

    public TrainArrivalBatchEvent(List<TrainArrivalEvent> arrivals) {
        this.arrivals = arrivals;
        this.timestamp = System.currentTimeMillis();
    }

    public TrainArrivalBatchEvent() {
    }

    public List<TrainArrivalEvent> getArrivals() {
        return this.arrivals;
    }

    public void setArrivals(List<TrainArrivalEvent> arrivals) {
        this.arrivals = arrivals;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TrainArrivalBatchEvent)) return false;
        final TrainArrivalBatchEvent other = (TrainArrivalBatchEvent) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$arrivals = this.getArrivals();
        final Object other$arrivals = other.getArrivals();
        if (this$arrivals == null ? other$arrivals != null : !this$arrivals.equals(other$arrivals)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TrainArrivalBatchEvent;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $arrivals = this.getArrivals();
        result = result * PRIME + ($arrivals == null ? 43 : $arrivals.hashCode());
        return result;
    }

    public String toString() {
        return "TrainArrivalBatchEvent(arrivals=" + this.getArrivals() + ")";
    }

    public long getTimestamp() {
        return this.timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
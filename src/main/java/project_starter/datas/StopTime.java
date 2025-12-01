package project_starter.datas;

import java.time.LocalTime;

public class StopTime {
    private String tripId;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private String stopId;
    private int stopSequence;
    private String stopHeadsign;
    private int pickupType;
    private int dropOffType;
    private double shapeDistTraveled;
    private int timepoint;

    public StopTime(String tripId, LocalTime arrivalTime, LocalTime departureTime,
                    String stopId, int stopSequence, String stopHeadsign,
                    int pickupType, int dropOffType, double shapeDistTraveled, int timepoint) {
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.stopHeadsign = stopHeadsign;
        this.pickupType = pickupType;
        this.dropOffType = dropOffType;
        this.shapeDistTraveled = shapeDistTraveled;
        this.timepoint = timepoint;
    }

    // -------- Getters --------
    public String getTripId() { return tripId; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    public LocalTime getDepartureTime() { return departureTime; }
    public String getStopId() { return stopId; }
    public int getStopSequence() { return stopSequence; }
    public String getStopHeadsign() { return stopHeadsign; }
    public int getPickupType() { return pickupType; }
    public int getDropOffType() { return dropOffType; }
    public double getShapeDistTraveled() { return shapeDistTraveled; }
    public int getTimepoint() { return timepoint; }

    @Override
    public String toString() {
        return tripId + " @ " + stopId + " → " + arrivalTime;
    }
}

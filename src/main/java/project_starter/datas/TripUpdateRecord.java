package project_starter.datas;

public class TripUpdateRecord {
    private final String tripId;
    private final String stopId;
    private final long arrivalEpochSeconds;

    public TripUpdateRecord(String tripId, String stopId, long arrivalEpochSeconds) {
        this.tripId = tripId;
        this.stopId = stopId;
        this.arrivalEpochSeconds = arrivalEpochSeconds;
    }

    public String getTripId() { return tripId; }
    public String getStopId() { return stopId; }
    public long getArrivalEpochSeconds() { return arrivalEpochSeconds; }
}

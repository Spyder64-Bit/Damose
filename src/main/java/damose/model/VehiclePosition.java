package damose.model;

import org.jxmapviewer.viewer.GeoPosition;

public class VehiclePosition {

    private final String tripId;
    private final String vehicleId;
    private final GeoPosition position;
    private final int stopSequence;
    private final String routeId;
    private final int directionId;

    public VehiclePosition(String tripId, String vehicleId, GeoPosition position, int stopSequence) {
        this(tripId, vehicleId, position, stopSequence, null, -1);
    }

    public VehiclePosition(String tripId, String vehicleId, GeoPosition position, int stopSequence,
                           String routeId, int directionId) {
        this.tripId = tripId;
        this.vehicleId = vehicleId;
        this.position = position;
        this.stopSequence = stopSequence;
        this.routeId = routeId;
        this.directionId = directionId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public GeoPosition getPosition() {
        return position;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public String getRouteId() {
        return routeId;
    }

    public int getDirectionId() {
        return directionId;
    }

    @Override
    public String toString() {
        return "Bus " + vehicleId + " trip=" + tripId +
               " route=" + routeId +
               " dir=" + directionId +
               " pos=" + position.getLatitude() + "," + position.getLongitude() +
               " stopSeq=" + stopSequence;
    }
}

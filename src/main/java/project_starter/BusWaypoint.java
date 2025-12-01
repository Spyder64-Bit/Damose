package project_starter;

import org.jxmapviewer.viewer.DefaultWaypoint;

import project_starter.model.VehiclePosition;  

public class BusWaypoint extends DefaultWaypoint {
    private final String tripId;
    private final String tripHeadsign;
    private final String vehicleId;

    // Costruttore che prende direttamente un VehiclePosition
    public BusWaypoint(VehiclePosition vp, String tripHeadsign) {
        super(vp.getPosition()); // posizione reale del bus
        this.tripId = vp.getTripId();
        this.vehicleId = vp.getVehicleId();
        this.tripHeadsign = tripHeadsign;
    }

    public String getTripId() {
        return tripId;
    }

    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public String toString() {
        return "Bus " + vehicleId + " su linea " + tripHeadsign + " (" + tripId + ")";
    }
}

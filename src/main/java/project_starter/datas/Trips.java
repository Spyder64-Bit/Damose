package project_starter.datas;

public class Trips {
    private String routeId;
    private String serviceId;
    private String tripId;
    private String tripHeadsign;
    private String tripShortName;   // 🔎 sostituito blockId con tripShortName
    private int directionId;
    private String shapeId;

    public Trips(String routeId, String serviceId, String tripId,
                 String tripHeadsign, String tripShortName, int directionId, String shapeId) {
        this.routeId = routeId;
        this.serviceId = serviceId;
        this.tripId = tripId;
        this.tripHeadsign = tripHeadsign;
        this.tripShortName = tripShortName;
        this.directionId = directionId;
        this.shapeId = shapeId;
    }

    // Getter
    public String getRouteId() { return routeId; }
    public String getServiceId() { return serviceId; }
    public String getTripId() { return tripId; }
    public String getTripHeadsign() { return tripHeadsign; }
    public String getTripShortName() { return tripShortName; }   // 🔎 aggiunto
    public int getDirectionId() { return directionId; }
    public String getShapeId() { return shapeId; }
}

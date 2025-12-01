package project_starter.datas;

public class Stops {
    private String stopId;
    private String stopCode;
    private String stopName;
    private double stopLat;
    private double stopLon;

    // 🔎 flag per distinguere fermate reali da linee fittizie
    private boolean isFakeLine = false;

    public Stops(String stopId, String stopCode, String stopName, double stopLat, double stopLon) {
        this.stopId = stopId;
        this.stopCode = stopCode;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
    }

    // -------- Accessors --------
    public String getStopId() { return stopId; }
    public String getStopCode() { return stopCode; }
    public String getStopName() { return stopName; }
    public double getStopLat() { return stopLat; }
    public double getStopLon() { return stopLon; }

    // -------- Linee fittizie --------
    public void markAsFakeLine() { this.isFakeLine = true; }
    public boolean isFakeLine() { return isFakeLine; }

    @Override
    public String toString() {
        // Fermate reali → "StopId - Nome"
        // Linee fittizie → solo "Nome"
        return isFakeLine ? stopName : stopId + " - " + stopName;
    }
}

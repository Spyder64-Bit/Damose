package project_starter.datas;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import project_starter.model.VehiclePosition;

public class TripMatcher {

    // Mappa tripId → Trips
    private final Map<String, Trips> tripsById;

    public TripMatcher(List<Trips> trips) {
        this.tripsById = trips.stream()
                .collect(Collectors.toMap(Trips::getTripId, t -> t));
    }

    /** Collega un bus realtime al suo trip statico */
    public Trips match(VehiclePosition vp) {
        return tripsById.get(vp.getTripId());
    }

    /** Recupera un Trips dato un tripId (usato per StopTimes) */
    public Trips matchByTripId(String tripId) {
        return tripsById.get(tripId);
    }

    /** Ricerca linee per route_id, headsign o short_name */
    public List<Trips> searchByRouteOrHeadsign(String query) {
        String q = query.toLowerCase();
        return tripsById.values().stream()
                .filter(t -> t.getRouteId().toLowerCase().contains(q)
                          || t.getTripHeadsign().toLowerCase().contains(q)
                          || (t.getTripShortName() != null && t.getTripShortName().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }
}

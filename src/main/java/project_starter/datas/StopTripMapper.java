package project_starter.datas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopTripMapper {
    private final Map<String, List<StopTime>> stopToTrips = new HashMap<>();
    private final Map<String, Map<Integer, String>> tripSeqToStop = new HashMap<>();
    private final Set<String> knownStopIds = new HashSet<>();
    private final TripMatcher matcher;   // usa la tua classe TripMatcher

    public StopTripMapper(List<StopTime> stopTimes, TripMatcher matcher) {
        this.matcher = matcher;
        for (StopTime st : stopTimes) {
            String stopId = st.getStopId();
            String tripId = st.getTripId();
            int seq = st.getStopSequence();

            knownStopIds.add(stopId);

            stopToTrips.computeIfAbsent(stopId, k -> new ArrayList<>()).add(st);
            tripSeqToStop.computeIfAbsent(tripId, k -> new HashMap<>()).put(seq, stopId);
        }
    }

    /** Restituisce tutte le corse (Trips) che passano per una fermata */
    public List<Trips> getTripsForStop(String stopId) {
        List<StopTime> times = stopToTrips.getOrDefault(stopId, Collections.emptyList());
        List<Trips> result = new ArrayList<>();
        for (StopTime st : times) {
            Trips trip = matcher.matchByTripId(st.getTripId());
            if (trip != null) result.add(trip);
        }
        return result;
    }

    /** True se lo stopId esiste nello statico */
    public boolean isKnownStopId(String stopId) {
        return knownStopIds.contains(stopId);
    }

    /** Restituisce lo stopId statico dato un tripId e una stop_sequence */
    public String getStopIdByTripAndSequence(String tripId, int sequence) {
        Map<Integer, String> seqMap = tripSeqToStop.get(tripId);
        if (seqMap == null) return null;
        return seqMap.get(sequence);
    }
}

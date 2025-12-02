package project_starter.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import project_starter.datas.StopTime;
import project_starter.datas.Stops;
import project_starter.datas.Trips;

/**
 * Service per gestire le operazioni relative ai percorsi delle linee.
 * Permette di ottenere la sequenza ordinata di fermate per una linea.
 */
public class RouteService {

    private final List<Trips> trips;
    private final List<StopTime> stopTimes;
    private final Map<String, Stops> stopsById;

    public RouteService(List<Trips> trips, List<StopTime> stopTimes, List<Stops> stops) {
        this.trips = trips;
        this.stopTimes = stopTimes;
        this.stopsById = stops.stream()
                .collect(Collectors.toMap(Stops::getStopId, s -> s, (a, b) -> a));
    }

    /**
     * Trova un trip rappresentativo per una linea (routeId + headsign).
     * Restituisce il primo trip che corrisponde.
     */
    public Trips findRepresentativeTrip(String routeId, String headsign) {
        return trips.stream()
                .filter(t -> t.getRouteId().equalsIgnoreCase(routeId))
                .filter(t -> headsign == null || t.getTripHeadsign().equalsIgnoreCase(headsign))
                .findFirst()
                .orElse(null);
    }

    /**
     * Trova tutti i trips per un routeId.
     */
    public List<Trips> findTripsByRouteId(String routeId) {
        return trips.stream()
                .filter(t -> t.getRouteId().equalsIgnoreCase(routeId))
                .collect(Collectors.toList());
    }

    /**
     * Ottiene la lista ordinata di fermate per un trip specifico.
     * Le fermate sono ordinate per stop_sequence.
     */
    public List<Stops> getStopsForTrip(String tripId) {
        if (tripId == null) return Collections.emptyList();

        List<StopTime> tripStopTimes = stopTimes.stream()
                .filter(st -> st.getTripId().equals(tripId))
                .sorted(Comparator.comparingInt(StopTime::getStopSequence))
                .collect(Collectors.toList());

        List<Stops> orderedStops = new ArrayList<>();
        for (StopTime st : tripStopTimes) {
            Stops stop = stopsById.get(st.getStopId());
            if (stop != null) {
                orderedStops.add(stop);
            }
        }

        return orderedStops;
    }

    /**
     * Ottiene la lista ordinata di fermate per una linea (routeId).
     * Usa il trip con più fermate come rappresentativo.
     */
    public List<Stops> getStopsForRoute(String routeId) {
        if (routeId == null) return Collections.emptyList();

        // Trova tutti i trip di questa linea
        List<Trips> routeTrips = findTripsByRouteId(routeId);
        if (routeTrips.isEmpty()) return Collections.emptyList();

        // Trova il trip con più fermate (più rappresentativo del percorso completo)
        String bestTripId = null;
        int maxStops = 0;

        for (Trips trip : routeTrips) {
            long count = stopTimes.stream()
                    .filter(st -> st.getTripId().equals(trip.getTripId()))
                    .count();
            if (count > maxStops) {
                maxStops = (int) count;
                bestTripId = trip.getTripId();
            }
        }

        if (bestTripId == null) return Collections.emptyList();

        return getStopsForTrip(bestTripId);
    }

    /**
     * Ottiene la lista ordinata di fermate per una linea con direzione specifica.
     */
    public List<Stops> getStopsForRouteAndHeadsign(String routeId, String headsign) {
        Trips trip = findRepresentativeTrip(routeId, headsign);
        if (trip == null) return Collections.emptyList();
        return getStopsForTrip(trip.getTripId());
    }

    /**
     * Restituisce tutte le direzioni (headsigns) disponibili per una linea.
     */
    public List<String> getHeadsignsForRoute(String routeId) {
        return trips.stream()
                .filter(t -> t.getRouteId().equalsIgnoreCase(routeId))
                .map(Trips::getTripHeadsign)
                .distinct()
                .collect(Collectors.toList());
    }
}


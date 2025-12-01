package project_starter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jxmapviewer.viewer.GeoPosition;

import project_starter.datas.StopTime;
import project_starter.datas.StopTimesLoader;
import project_starter.datas.Stops;
import project_starter.datas.StopsLoader;
import project_starter.datas.Trips;
import project_starter.datas.TripsLoader;
import project_starter.model.VehiclePosition;   

public class StaticSimulator {

    /**
     * Simula tutti i bus presenti nei file statici GTFS.
     * Per ogni tripId in trips.txt, associa tutte le fermate da stop_times.txt
     * e crea un VehiclePosition con la posizione della fermata.
     */
    public static List<VehiclePosition> simulateAllTrips() {
        List<VehiclePosition> buses = new ArrayList<>();

        // Carica i dati statici
        List<Trips> trips = TripsLoader.load("/gtfs_static/trips.txt");
        List<StopTime> stopTimes = StopTimesLoader.load("/gtfs_static/stop_times.txt");
        List<Stops> stops = StopsLoader.load("/gtfs_static/stops.txt");

        // Per ogni trip
        for (Trips trip : trips) {
            String tripId = trip.getTripId();

            // Trova tutte le fermate di quel trip ordinate per sequenza
            List<StopTime> tripStops = stopTimes.stream()
                    .filter(st -> st.getTripId().equals(tripId))
                    .sorted((a, b) -> Integer.compare(a.getStopSequence(), b.getStopSequence()))
                    .collect(Collectors.toList());

            // Per ogni fermata del trip
            for (StopTime st : tripStops) {
                Stops stop = stops.stream()
                        .filter(s -> s.getStopId().equals(st.getStopId()))
                        .findFirst()
                        .orElse(null);

                if (stop != null) {
                    GeoPosition pos = new GeoPosition(stop.getStopLat(), stop.getStopLon());

                    buses.add(new VehiclePosition(
                            tripId,
                            "SIM-" + tripId, // id fittizio del bus
                            pos,
                            st.getStopSequence()
                    ));
                }
            }
        }

        return buses;
    }
}

package project_starter.controller;

import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.jxmapviewer.viewer.GeoPosition;

import project_starter.ConnectionMode;
import project_starter.SimpleDocumentListener;
import project_starter.StaticSimulator;
import project_starter.datas.StopTime;
import project_starter.datas.StopTimesLoader;
import project_starter.datas.StopTripMapper;
import project_starter.datas.Stops;
import project_starter.datas.StopsLoader;
import project_starter.datas.TripMatcher;
import project_starter.datas.TripUpdateRecord;
import project_starter.datas.Trips;
import project_starter.datas.TripsLoader;
import project_starter.model.GTFSFetcher;
import project_starter.model.VehiclePosition;
import project_starter.view.MapOverlayUpdater;
import project_starter.view.RealTimeBusTrackerView;

/**
 * Controller principale: gestisce fermate, linee, overlay mappa e pannello flottante.
 * Mostra dati realtime (ONLINE) confrontati con gli orari statici, oppure solo statici (OFFLINE).
 */
public class RealTimeBusTrackerController {

    private List<Stops> fermate;
    private List<Trips> trips;
    private TripMatcher matcher;
    private StopTripMapper stopTripMapper;
    private ConnectionMode mode = ConnectionMode.ONLINE;
    private RealTimeBusTrackerView view;


    // Mappa realtime: tripId -> (stopId -> arrivalEpochSeconds)
    private final Map<String, Map<String, Long>> realtimeArrivals = new HashMap<>();
    private Timer realtimeTimer;

public void start() {
    System.out.println("Avvio applicazione...");

    // Carico fermate e corse statiche
    fermate = StopsLoader.load("/gtfs_static/stops.txt");
    trips = TripsLoader.load("/gtfs_static/trips.txt");

    // Creo il matcher per collegare tripId → Trips
    matcher = new TripMatcher(trips);

    // Carico stop_times e costruisco il mapper con il matcher
    List<StopTime> stopTimes = StopTimesLoader.load("/gtfs_static/stop_times.txt");
    stopTripMapper = new StopTripMapper(stopTimes, matcher);

    // Inizializzo la view
    view = new RealTimeBusTrackerView();
    view.init();
    view.setAllStops(fermate);

    setupSearchPanel();

    // Listener click su fermata
    view.setStopClickListener(stop -> {
        if (stop == null) return;
        view.setStopInfo(stop);

        if (!stop.isFakeLine()) {
            List<Trips> linee = stopTripMapper.getTripsForStop(stop.getStopId());
            if (linee != null && !linee.isEmpty()) {
                String infoLinee = linee.stream()
                    .map(t -> t.getRouteId() + " - " + t.getTripHeadsign())
                    .distinct()
                    .collect(Collectors.joining(", "));
                view.showLinesInfo("Linee che passano: " + infoLinee);
            } else {
                view.showLinesInfo("Nessuna linea trovata");
            }

            centerOnStop(stop);
            showFloatingArrivals(stop);
        } else {
            view.showLinesInfo("Linea selezionata: " + stop.getStopName());
        }
    });

    view.addWaypointClickListener();

    MapOverlayUpdater.updateMap(view.getMapViewer(), fermate, List.of(), trips);

    startRealtimeUpdates();

    System.out.println("Applicazione avviata correttamente");
}


    private void setupSearchPanel() {
        view.getSearchButton().addActionListener(e -> view.toggleSidePanel());

        view.getSearchField().getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            String query = view.getSearchField().getText().trim().toLowerCase();
            filterStopsAndLines(query);
        }));

        view.getStopList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Stops stop = view.getStopList().getSelectedValue();
                if (stop == null) return;

                view.setStopInfo(stop);

                if (!stop.isFakeLine()) {
                    List<Trips> linee = stopTripMapper.getTripsForStop(stop.getStopId());
                    if (linee != null && !linee.isEmpty()) {
                        String infoLinee = linee.stream()
                            .map(t -> t.getRouteId() + " - " + t.getTripHeadsign())
                            .distinct()
                            .collect(Collectors.joining(", "));
                        view.showLinesInfo("Linee che passano: " + infoLinee);
                    } else {
                        view.showLinesInfo("Nessuna linea trovata");
                    }

                    centerOnStop(stop);
                    showFloatingArrivals(stop);

                } else {
                    view.showLinesInfo("Linea selezionata: " + stop.getStopName());
                }
            }
        });
    }

    private void filterStopsAndLines(String query) {
        view.clearStopList();
        if (query.isEmpty()) return;

        if (view.isStopsMode()) {
            fermate.stream()
                .filter(s -> s.getStopName().toLowerCase().contains(query)
                          || s.getStopId().toLowerCase().contains(query))
                .limit(100)
                .forEach(view::addStopToList);

        } else if (view.isLinesMode()) {
            List<Trips> matchingTrips = trips.stream()
                .filter(t -> t.getRouteId().toLowerCase().contains(query))
                .collect(Collectors.toList());

            matchingTrips.stream()
                .map(t -> t.getRouteId() + " - " + t.getTripHeadsign())
                .distinct()
                .forEach(lineName -> {
                    Stops cleanLine = new Stops(
                        "fake-" + lineName.replace(" ", ""),
                        "",
                        lineName,
                        0.0,
                        0.0
                    );
                    cleanLine.markAsFakeLine();
                    view.addStopToList(cleanLine);
                });
        }
    }

    private void centerOnStop(Stops stop) {
        if (stop.getStopLat() == 0.0 && stop.getStopLon() == 0.0) return;
        GeoPosition pos = new GeoPosition(stop.getStopLat(), stop.getStopLon());
        view.getMapViewer().setAddressLocation(pos);
        view.getMapViewer().setZoom(1); // come hai impostato
    }

    // ============================
    // Mostra arrivi realtime vs statici
    // ============================
    private void showFloatingArrivals(Stops stop) {
        List<StopTime> times = StopTimesLoader.getStopTimesForStop(stop.getStopId());
        if (times == null || times.isEmpty()) {
            showPanel(stop, List.of("Nessun arrivo imminente"));
            return;
        }

        Map<String, StopTime> primiArrivi = new HashMap<>();

        for (StopTime st : times) {
            Trips trip = matcher.matchByTripId(st.getTripId());
            if (trip == null) continue;
            String routeId = trip.getRouteId();

           Long predicted = (mode == ConnectionMode.ONLINE)
        ? lookupRealtimeArrivalEpoch(st.getTripId(), st.getStopId())
        : null;
        if (predicted != null) {
         System.out.println("Predizione trovata: route=" + routeId + " trip=" + st.getTripId() + " stop=" + st.getStopId());
        } else {
    System.out.println("Realtime assente per: route=" + routeId + " trip=" + st.getTripId() + " stop=" + st.getStopId());
        }

            if (predicted != null) {
                StopTime esistente = primiArrivi.get(routeId);
                if (esistente == null) {
                    primiArrivi.put(routeId, st);
                } else {
                    Long esPred = lookupRealtimeArrivalEpoch(esistente.getTripId(), esistente.getStopId());
                    if (esPred == null || predicted < esPred) {
                        primiArrivi.put(routeId, st);
                    }
                }
            } else {
                // fallback statico
                LocalTime arr = st.getArrivalTime();
                if (arr == null) continue;
                long diff = Duration.between(LocalTime.now(), arr).toMinutes();
                if (diff < 0 || diff > 60) continue;
                StopTime esistente = primiArrivi.get(routeId);
                if (esistente == null || arr.isBefore(esistente.getArrivalTime())) {
                    primiArrivi.put(routeId, st);
                }
            }
        }

        List<String> arrivi = primiArrivi.entrySet().stream()
            .map(entry -> {
                StopTime st = entry.getValue();
                Trips trip = matcher.matchByTripId(st.getTripId());
                if (trip == null) return null;
                Long predicted = (mode == ConnectionMode.ONLINE)
                        ? lookupRealtimeArrivalEpoch(st.getTripId(), st.getStopId())
                        : null;
                return buildArrivalInfo(trip, st, predicted);
            })
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList());

        if (arrivi.isEmpty()) arrivi.add("Nessun arrivo imminente");
        showPanel(stop, arrivi);
    }

    private String buildArrivalInfo(Trips trip, StopTime st, Long predictedEpoch) {
        LocalTime scheduled = st.getArrivalTime();
        if (scheduled == null) return trip.getRouteId() + " - orario non disponibile";

        Instant scheduledInstant = scheduled.atDate(LocalDate.now())
                .atZone(ZoneId.systemDefault())
                .toInstant();
        long scheduledEpoch = scheduledInstant.getEpochSecond();
        long nowEpoch = Instant.now().getEpochSecond();

        if (mode == ConnectionMode.ONLINE && predictedEpoch != null) {
            long delaySec = predictedEpoch - scheduledEpoch;
            long delayMin = delaySec / 60;
            long diffMin = Math.max(0, (predictedEpoch - nowEpoch) / 60);

            String status;
            if (delayMin > 1) {
                status = "ritardo di " + delayMin + " min";
            } else if (delayMin < -1) {
                status = "anticipo di " + Math.abs(delayMin) + " min";
            } else {
                status = "in orario";
            }

            return trip.getRouteId() + " - " + (diffMin == 0 ? "In arrivo" : diffMin + " min") + " (" + status + ")";
        } else {
            long diffMin = Duration.between(LocalTime.now(), scheduled).toMinutes();
            return trip.getRouteId() + " - " + (diffMin == 0 ? "In arrivo" : diffMin + " min") + " (statico)";
        }
    }

    private void showPanel(Stops stop, List<String> arrivi) {
        GeoPosition anchorGeo = new GeoPosition(stop.getStopLat(), stop.getStopLon());
        Point2D p2d = view.getMapViewer().convertGeoPositionToPoint(anchorGeo);
        SwingUtilities.invokeLater(() -> {
            view.showFloatingPanel(stop.getStopName(), arrivi, p2d, anchorGeo);
            // Se la view chiama fadeIn internamente, il pannello sfumerà in apertura.
            // Altrimenti, puoi aggiungere un metodo pubblico nella view per triggerare il fade.
        });
    }

    // ============================
    // Realtime: TripUpdates + vehicle positions
    // ============================
    private void startRealtimeUpdates() {
        if (realtimeTimer != null) {
            realtimeTimer.cancel();
        }
        realtimeTimer = new Timer("realtime-updates", true);

realtimeTimer.scheduleAtFixedRate(new TimerTask() {
    @Override
    public void run() {
        // 1) TripUpdates (ONLINE)
        if (mode == ConnectionMode.ONLINE) {
            try {
                List<TripUpdateRecord> updates = GTFSFetcher.fetchTripUpdates(stopTripMapper);
                System.out.println("TripUpdates ricevuti: " + updates.size());
                updates.stream().limit(5).forEach(u ->
                System.out.println("TU: trip=" + u.getTripId() + " stop=" + u.getStopId() + " t=" + u.getArrivalEpochSeconds())
                );
                synchronized (realtimeArrivals) {
                    realtimeArrivals.clear();
                    for (TripUpdateRecord u : updates) {
                        realtimeArrivals
                            .computeIfAbsent(u.getTripId(), k -> new HashMap<>())
                            .put(u.getStopId(), u.getArrivalEpochSeconds());
                    }
                }
            } catch (Exception ex) {
                System.out.println("Impossibile ottenere TripUpdates realtime: " + ex.getMessage());
            }
        }

        // 2) Vehicle positions (overlay mappa)
        List<VehiclePosition> computedPositions;
        try {
            computedPositions = (mode == ConnectionMode.ONLINE)
                ? GTFSFetcher.fetchBusPositions()
                : StaticSimulator.simulateAllTrips();
        } catch (Exception e) {
            mode = ConnectionMode.OFFLINE;
            computedPositions = StaticSimulator.simulateAllTrips();
        }

        // rende la variabile final per la lambda
        final List<VehiclePosition> busPositions = computedPositions;

        SwingUtilities.invokeLater(() ->
            MapOverlayUpdater.updateMap(view.getMapViewer(), fermate, busPositions, trips)
        );
    }
}, 0, 15000);
 // ogni 15s
    }

    private Long lookupRealtimeArrivalEpoch(String tripId, String stopId) {
        Map<String, Long> byStop = realtimeArrivals.get(tripId);
        return byStop != null ? byStop.get(stopId) : null;
    }
}

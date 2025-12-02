package project_starter.controller;

import java.awt.geom.Point2D;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.jxmapviewer.viewer.GeoPosition;

import com.google.transit.realtime.GtfsRealtime;

import project_starter.datas.CalendarLoader;
import project_starter.datas.StopTime;
import project_starter.datas.StopTimesLoader;
import project_starter.datas.StopTripMapper;
import project_starter.datas.Stops;
import project_starter.datas.StopsLoader;
import project_starter.datas.TripMatcher;
import project_starter.datas.TripServiceCalendar;
import project_starter.datas.TripUpdateRecord;
import project_starter.datas.Trips;
import project_starter.datas.TripsLoader;
import project_starter.model.ConnectionMode;
import project_starter.model.GTFSFetcher;
import project_starter.model.StaticSimulator;
import project_starter.model.VehiclePosition;
import project_starter.view.MapOverlayUpdater;
import project_starter.view.RealTimeBusTrackerView;
import project_starter.view.SimpleDocumentListener;

/**
 * Controller principale dell'applicazione Rome Bus Tracker.
 * Coordina la view, i dati statici GTFS e gli aggiornamenti real-time.
 */
public class RealTimeBusTrackerController {

    private List<Stops> fermate;
    private List<Trips> trips;
    private List<StopTime> stopTimes;
    private TripMatcher matcher;
    private StopTripMapper stopTripMapper;
    private RouteService routeService;
    private ConnectionMode mode = ConnectionMode.ONLINE;
    private RealTimeBusTrackerView view;

    private ArrivalService arrivalService;
    private Timer realtimeTimer;
    private long currentFeedTs = Instant.now().getEpochSecond();

    // ============================
    // Avvio applicazione
    // ============================
    public void start() {
        System.out.println("Avvio applicazione...");

        // Carico statici
        fermate = StopsLoader.load("/gtfs_static/stops.txt");
        trips = TripsLoader.load("/gtfs_static/trips.txt");
        stopTimes = StopTimesLoader.load("/gtfs_static/stop_times.txt");

        System.out.println("Stops caricati: " + (fermate == null ? 0 : fermate.size()));
        System.out.println("Trips caricati: " + (trips == null ? 0 : trips.size()));

        matcher = new TripMatcher(trips);
        stopTripMapper = new StopTripMapper(stopTimes, matcher);
        routeService = new RouteService(trips, stopTimes, fermate);

        // Carica calendar_dates.txt
        TripServiceCalendar tripServiceCalendar;
        try {
            tripServiceCalendar = CalendarLoader.loadFromCalendarDates("/gtfs_static/calendar_dates.txt");
        } catch (Exception e) {
            System.out.println("Impossibile caricare calendar_dates: " + e.getMessage());
            tripServiceCalendar = new TripServiceCalendar();
        }

        // Inizializza ArrivalService
        arrivalService = new ArrivalService(matcher, stopTripMapper, tripServiceCalendar);

        // Inizializza view
        view = new RealTimeBusTrackerView();
        view.init();
        view.setAllStops(fermate);

        setupSearchPanel();
        setupStopClickListener();

        view.addWaypointClickListener();
        // Mappa iniziale vuota (nessuna fermata visibile)
        MapOverlayUpdater.updateMap(view.getMapViewer(), Collections.emptyList(), Collections.emptyList(), trips);

        // Avvio GestoreRealTime
        GestoreRealTime.setMode(mode);
        GestoreRealTime.startAggiornamento();

        startRealtimeUpdates();

        System.out.println("Applicazione avviata correttamente");
    }

    // ============================
    // Setup listeners
    // ============================
    private void setupStopClickListener() {
        view.setStopClickListener(stop -> {
            if (stop == null) return;
            handleStopSelection(stop);
        });
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
                if (stop != null) {
                    handleStopSelection(stop);
                }
            }
        });
    }

    private void handleStopSelection(Stops stop) {
        if (stop.isFakeLine()) {
            // È una linea: mostra il percorso
            handleLineSelection(stop);
        } else {
            // È una fermata: centra e mostra solo questa fermata
            MapOverlayUpdater.clearRoute();
            MapOverlayUpdater.setVisibleStops(Collections.singletonList(stop));
            centerOnStop(stop);
            showFloatingArrivals(stop);
            refreshMapOverlay();
        }
    }

    private void handleLineSelection(Stops fakeLine) {
        // Estrai routeId e headsign dal nome della linea fake
        // Formato: "routeId - headsign"
        String lineName = fakeLine.getStopName();
        String[] parts = lineName.split(" - ", 2);
        String routeId = parts[0].trim();
        String headsign = parts.length > 1 ? parts[1].trim() : null;

        System.out.println("Linea selezionata: " + routeId + " -> " + headsign);

        // Ottieni le fermate del percorso
        List<Stops> routeStops = routeService.getStopsForRouteAndHeadsign(routeId, headsign);

        if (routeStops.isEmpty()) {
            // Prova senza headsign
            routeStops = routeService.getStopsForRoute(routeId);
        }

        if (routeStops.isEmpty()) {
            System.out.println("Nessuna fermata trovata per la linea " + routeId);
            return;
        }

        System.out.println("Fermate trovate per " + routeId + ": " + routeStops.size());

        // Imposta il percorso sulla mappa
        MapOverlayUpdater.setRoute(routeStops);
        refreshMapOverlay();

        // Centra la mappa per mostrare tutto il percorso
        fitMapToRoute(routeStops);

        // Nascondi il pannello flottante degli arrivi
        view.hideFloatingPanel();
    }

    // ============================
    // Filtro ricerca
    // ============================
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

    // ============================
    // Navigazione mappa
    // ============================
    private void centerOnStop(Stops stop) {
        if (stop.getStopLat() == 0.0 && stop.getStopLon() == 0.0) return;
        GeoPosition pos = new GeoPosition(stop.getStopLat(), stop.getStopLon());
        view.getMapViewer().setAddressLocation(pos);
        view.getMapViewer().setZoom(1);
    }

    /**
     * Adatta la mappa per mostrare tutte le fermate del percorso.
     * Centra sulla fermata centrale e imposta zoom per vedere tutto il percorso.
     */
    private void fitMapToRoute(List<Stops> routeStops) {
        if (routeStops == null || routeStops.isEmpty()) return;

        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

        for (Stops s : routeStops) {
            minLat = Math.min(minLat, s.getStopLat());
            maxLat = Math.max(maxLat, s.getStopLat());
            minLon = Math.min(minLon, s.getStopLon());
            maxLon = Math.max(maxLon, s.getStopLon());
        }

        // Centra sulla fermata centrale del percorso (non sul bounding box)
        int middleIndex = routeStops.size() / 2;
        Stops middleStop = routeStops.get(middleIndex);
        view.getMapViewer().setAddressLocation(new GeoPosition(middleStop.getStopLat(), middleStop.getStopLon()));

        // Calcola zoom appropriato per vedere tutto il percorso
        // In JXMapViewer: valori più alti = più zoom out
        double latDiff = maxLat - minLat;
        double lonDiff = maxLon - minLon;
        double maxDiff = Math.max(latDiff, lonDiff);

        int zoom;
        if (maxDiff > 0.3) zoom = 8;       // Percorso molto lungo
        else if (maxDiff > 0.15) zoom = 7; // Percorso lungo
        else if (maxDiff > 0.08) zoom = 6; // Percorso medio-lungo
        else if (maxDiff > 0.04) zoom = 5; // Percorso medio
        else if (maxDiff > 0.02) zoom = 4; // Percorso corto
        else zoom = 3;                      // Percorso molto corto

        view.getMapViewer().setZoom(zoom);
    }

    // ============================
    // Mostra arrivi (delegato ad ArrivalService)
    // ============================
    private void showFloatingArrivals(Stops stop) {
        List<String> arrivi = arrivalService.computeArrivalsForStop(stop.getStopId(), mode, currentFeedTs);
        showPanel(stop, arrivi);
    }

    private void showPanel(Stops stop, List<String> arrivi) {
        GeoPosition anchorGeo = new GeoPosition(stop.getStopLat(), stop.getStopLon());
        Point2D p2d = view.getMapViewer().convertGeoPositionToPoint(anchorGeo);
        SwingUtilities.invokeLater(() -> view.showFloatingPanel(stop.getStopName(), arrivi, p2d, anchorGeo));
    }

    // ============================
    // Refresh map
    // ============================
    private void refreshMapOverlay() {
        GtfsRealtime.FeedMessage vpFeed = GestoreRealTime.getLatestVehiclePositions();
        List<VehiclePosition> positions;
        try {
            positions = (mode == ConnectionMode.ONLINE)
                ? GTFSFetcher.parseVehiclePositions(vpFeed)
                : StaticSimulator.simulateAllTrips();
        } catch (Exception e) {
            positions = Collections.emptyList();
        }

        final List<VehiclePosition> busPositions = positions;
        // Passa lista vuota per allStops - le fermate visibili sono gestite da setVisibleStops/setRoute
        SwingUtilities.invokeLater(() -> MapOverlayUpdater.updateMap(view.getMapViewer(), Collections.emptyList(), busPositions, trips));
    }

    // ============================
    // Realtime updates
    // ============================
    private void startRealtimeUpdates() {
        if (realtimeTimer != null) {
            realtimeTimer.cancel();
        }
        realtimeTimer = new Timer("realtime-updates", true);

        realtimeTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                GtfsRealtime.FeedMessage tuFeed = GestoreRealTime.getLatestTripUpdates();
                GtfsRealtime.FeedMessage vpFeed = GestoreRealTime.getLatestVehiclePositions();

                // Aggiorna timestamp feed
                try {
                    long tsTU = (tuFeed != null && tuFeed.hasHeader() && tuFeed.getHeader().hasTimestamp())
                                ? tuFeed.getHeader().getTimestamp()
                                : Instant.now().getEpochSecond();
                    currentFeedTs = tsTU;
                } catch (Exception ignored) {}

                // Aggiorna dati RT
                if (mode == ConnectionMode.ONLINE) {
                    try {
                        List<TripUpdateRecord> updates = GTFSFetcher.parseTripUpdates(tuFeed, stopTripMapper, currentFeedTs);
                        arrivalService.updateRealtimeArrivals(updates);
                    } catch (Exception ex) {
                        System.out.println("Errore parsing TripUpdates RT: " + ex.getMessage());
                    }
                }

                // Vehicle positions / overlay
                List<VehiclePosition> computedPositions;
                try {
                    computedPositions = (mode == ConnectionMode.ONLINE)
                        ? GTFSFetcher.parseVehiclePositions(vpFeed)
                        : StaticSimulator.simulateAllTrips();
                } catch (Exception e) {
                    mode = ConnectionMode.OFFLINE;
                    computedPositions = StaticSimulator.simulateAllTrips();
                }

                final List<VehiclePosition> busPositions = computedPositions;
                // Passa lista vuota per allStops - le fermate visibili sono gestite separatamente
                SwingUtilities.invokeLater(() -> MapOverlayUpdater.updateMap(view.getMapViewer(), Collections.emptyList(), busPositions, trips));
            }
        }, 0, 30_000); // Update every 30 seconds
    }
}

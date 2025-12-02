package project_starter.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.WaypointPainter;

import project_starter.datas.Stops;
import project_starter.datas.Trips;
import project_starter.model.BusWaypoint;
import project_starter.model.StopWaypoint;
import project_starter.model.VehiclePosition;
import project_starter.render.BusWaypointRenderer;
import project_starter.render.RoutePainter;
import project_starter.render.StopWaypointRenderer;

/**
 * Classe responsabile dell'aggiornamento degli overlay sulla mappa.
 * Ottimizzato per evitare lag e glitch.
 */
public class MapOverlayUpdater {

    // Painter riutilizzabili
    private static WaypointPainter<StopWaypoint> stopPainter;
    private static WaypointPainter<BusWaypoint> busPainter;
    private static final RoutePainter routePainter = new RoutePainter();
    
    // Cache IDs per confronto veloce
    private static Set<String> currentStopIds = new HashSet<>();
    private static Set<String> currentBusIds = new HashSet<>();
    
    // Stato delle fermate visibili
    private static final List<Stops> routeStops = new ArrayList<>();
    private static final List<Stops> visibleStops = new ArrayList<>();
    
    // Lock per thread safety
    private static final Object lock = new Object();
    
    // Flag inizializzazione
    private static boolean initialized = false;
    private static JXMapViewer currentMap = null;

    /**
     * Inizializza i painter (chiamato una sola volta).
     */
    private static void initPainters(JXMapViewer mapViewer) {
        if (initialized && currentMap == mapViewer) return;
        
        stopPainter = new WaypointPainter<>();
        stopPainter.setRenderer(new StopWaypointRenderer());
        stopPainter.setWaypoints(new HashSet<>());
        
        busPainter = new WaypointPainter<>();
        busPainter.setRenderer(new BusWaypointRenderer());
        busPainter.setWaypoints(new HashSet<>());
        
        // Overlay registrato una sola volta
        mapViewer.setOverlayPainter((g, map, w, h) -> {
            synchronized (lock) {
                if (routePainter.hasRoute()) {
                    routePainter.paint(g, map, w, h);
                }
                stopPainter.paint(g, map, w, h);
                busPainter.paint(g, map, w, h);
            }
        });
        
        initialized = true;
        currentMap = mapViewer;
    }

    /**
     * Aggiorna la mappa con fermate e bus.
     */
    public static void updateMap(JXMapViewer mapViewer,
                                 List<Stops> allStops,
                                 List<VehiclePosition> busPositions,
                                 List<Trips> trips) {
        
        // Assicurati di essere sull'EDT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> updateMap(mapViewer, allStops, busPositions, trips));
            return;
        }
        
        initPainters(mapViewer);

        boolean needsRepaint = false;

        synchronized (lock) {
            // Calcola fermate da mostrare
            Set<String> showIds = new HashSet<>();
            for (Stops s : visibleStops) showIds.add(s.getStopId());
            for (Stops s : routeStops) showIds.add(s.getStopId());

            // Controlla se le fermate sono cambiate
            if (!showIds.equals(currentStopIds)) {
                currentStopIds = new HashSet<>(showIds);
                
                Set<StopWaypoint> newStopWaypoints = new HashSet<>();
                for (Stops s : visibleStops) newStopWaypoints.add(new StopWaypoint(s));
                for (Stops s : routeStops) newStopWaypoints.add(new StopWaypoint(s));
                
                stopPainter.setWaypoints(newStopWaypoints);
                needsRepaint = true;
            }

            // Calcola bus IDs
            Set<String> newBusIds = new HashSet<>();
            for (VehiclePosition vp : busPositions) {
                if (vp.getVehicleId() != null) newBusIds.add(vp.getVehicleId());
            }

            // Controlla se i bus sono cambiati
            if (!newBusIds.equals(currentBusIds)) {
                currentBusIds = newBusIds;
                
                Set<BusWaypoint> newBusWaypoints = new HashSet<>();
                for (VehiclePosition vp : busPositions) {
                    Trips trip = findTrip(trips, vp.getTripId());
                    String headsign = (trip != null) ? trip.getTripHeadsign() : vp.getTripId();
                    newBusWaypoints.add(new BusWaypoint(vp, headsign));
                }
                
                busPainter.setWaypoints(newBusWaypoints);
                needsRepaint = true;
            }
        }

        // Repaint solo se necessario
        if (needsRepaint) {
            mapViewer.repaint();
        }
    }

    private static Trips findTrip(List<Trips> trips, String tripId) {
        if (tripId == null || trips == null) return null;
        for (Trips t : trips) {
            if (t.getTripId().equals(tripId)) return t;
        }
        return null;
    }

    /**
     * Imposta le fermate visibili sulla mappa.
     */
    public static void setVisibleStops(List<Stops> stops) {
        synchronized (lock) {
            visibleStops.clear();
            if (stops != null) visibleStops.addAll(stops);
            currentStopIds.clear(); // Forza aggiornamento
        }
    }

    /**
     * Pulisce le fermate visibili.
     */
    public static void clearVisibleStops() {
        synchronized (lock) {
            visibleStops.clear();
            currentStopIds.clear();
        }
    }

    /**
     * Imposta il percorso da visualizzare sulla mappa.
     */
    public static void setRoute(List<Stops> stops) {
        synchronized (lock) {
            routeStops.clear();
            currentStopIds.clear(); // Forza aggiornamento
            
            if (stops == null || stops.size() < 2) {
                routePainter.clearRoute();
                return;
            }
            
            routeStops.addAll(stops);

            List<GeoPosition> positions = new ArrayList<>();
            for (Stops stop : stops) {
                positions.add(new GeoPosition(stop.getStopLat(), stop.getStopLon()));
            }
            routePainter.setRoute(positions);
        }
        
        // Forza repaint per mostrare il percorso
        if (currentMap != null) {
            currentMap.repaint();
        }
    }

    /**
     * Pulisce il percorso visualizzato.
     */
    public static void clearRoute() {
        synchronized (lock) {
            routePainter.clearRoute();
            routeStops.clear();
            currentStopIds.clear();
        }
    }

    /**
     * Verifica se c'è un percorso attivo.
     */
    public static boolean hasActiveRoute() {
        synchronized (lock) {
            return routePainter.hasRoute();
        }
    }

    /**
     * Pulisce tutto: percorso e fermate visibili.
     */
    public static void clearAll() {
        clearRoute();
        clearVisibleStops();
    }
}

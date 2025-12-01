package project_starter.view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import project_starter.datas.Stops;

/**
 * Utility per calcoli geografici e interazioni con la mappa
 */
public class GeoUtils {

    /**
     * Calcola la distanza tra due coordinate geografiche usando la formula di Haversine.
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // raggio della Terra in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.sin(dLon/2) * Math.sin(dLon/2) *
                   Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Verifica se un click è vicino a una fermata sulla mappa.
     */
    public static boolean isClickCloseToStop(JXMapViewer mapViewer, Stops stop, int clickX, int clickY) {
        var point = mapViewer.getTileFactory().geoToPixel(
                new GeoPosition(stop.getStopLat(), stop.getStopLon()),
                mapViewer.getZoom()
        );

        double dx = clickX - point.getX();
        double dy = clickY - point.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        return dist < 20; // soglia di prossimità
    }
}

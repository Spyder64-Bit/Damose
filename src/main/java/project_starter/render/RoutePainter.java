package project_starter.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Painter per disegnare il percorso di una linea sulla mappa.
 * Disegna una polyline che connette tutte le fermate in ordine.
 */
public class RoutePainter implements Painter<JXMapViewer> {

    private List<GeoPosition> route;
    private Color routeColor = new Color(220, 50, 50, 220);      // Rosso brillante
    private Color outlineColor = new Color(120, 20, 20, 255);    // Rosso scuro per bordo
    private float lineWidth = 5.0f;

    public RoutePainter() {
        this.route = new ArrayList<>();
    }

    public RoutePainter(List<GeoPosition> route) {
        this.route = route != null ? new ArrayList<>(route) : new ArrayList<>();
    }

    public void setRoute(List<GeoPosition> route) {
        this.route = route != null ? new ArrayList<>(route) : new ArrayList<>();
    }

    public void clearRoute() {
        this.route = new ArrayList<>();
    }

    public void setRouteColor(Color color) {
        this.routeColor = color;
    }

    public void setLineWidth(float width) {
        this.lineWidth = width;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        if (route == null || route.size() < 2) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Ottieni viewport per convertire coordinate mondo -> schermo
        Rectangle2D viewport = map.getViewportBounds();

        // Converti GeoPosition in punti schermo
        List<Point2D> screenPoints = new ArrayList<>();
        for (GeoPosition pos : route) {
            Point2D worldPt = map.getTileFactory().geoToPixel(pos, map.getZoom());
            // Converti da coordinate mondo a coordinate schermo
            int screenX = (int) (worldPt.getX() - viewport.getX());
            int screenY = (int) (worldPt.getY() - viewport.getY());
            screenPoints.add(new Point2D.Double(screenX, screenY));
        }

        // Disegna bordo (outline) più spesso
        g2.setColor(outlineColor);
        g2.setStroke(new BasicStroke(lineWidth + 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawPolyline(g2, screenPoints);

        // Disegna linea principale
        g2.setColor(routeColor);
        g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawPolyline(g2, screenPoints);

        // Disegna cerchietti alle fermate
        for (Point2D pt : screenPoints) {
            int size = 10;
            int x = (int) pt.getX() - size / 2;
            int y = (int) pt.getY() - size / 2;
            
            // Cerchio bianco con bordo rosso
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, size, size);
            g2.setColor(outlineColor);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x, y, size, size);
        }

        g2.dispose();
    }

    private void drawPolyline(Graphics2D g, List<Point2D> points) {
        if (points.size() < 2) return;
        
        int[] xPoints = new int[points.size()];
        int[] yPoints = new int[points.size()];

        for (int i = 0; i < points.size(); i++) {
            xPoints[i] = (int) points.get(i).getX();
            yPoints[i] = (int) points.get(i).getY();
        }

        g.drawPolyline(xPoints, yPoints, points.size());
    }

    public boolean hasRoute() {
        return route != null && route.size() >= 2;
    }
}

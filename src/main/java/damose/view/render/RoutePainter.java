package damose.view.render;

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

import damose.config.AppConstants;

public class RoutePainter implements Painter<JXMapViewer> {

    private List<GeoPosition> route;
    private Color routeColor = AppConstants.ROUTE_COLOR;
    private Color outlineColor = AppConstants.ROUTE_OUTLINE_COLOR;
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

        Rectangle2D viewport = map.getViewportBounds();

        List<Point2D> screenPoints = new ArrayList<>();
        for (GeoPosition pos : route) {
            Point2D worldPt = map.getTileFactory().geoToPixel(pos, map.getZoom());
            int screenX = (int) (worldPt.getX() - viewport.getX());
            int screenY = (int) (worldPt.getY() - viewport.getY());
            screenPoints.add(new Point2D.Double(screenX, screenY));
        }

        g2.setColor(outlineColor);
        g2.setStroke(new BasicStroke(lineWidth + 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawPolyline(g2, screenPoints);

        g2.setColor(routeColor);
        g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawPolyline(g2, screenPoints);

        drawRouteNodes(g2, screenPoints, map.getZoom());

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

    private void drawRouteNodes(Graphics2D g, List<Point2D> points, int zoom) {
        if (points == null || points.isEmpty()) return;

        int size;
        if (zoom >= 8) size = 4;
        else if (zoom >= 7) size = 6;
        else if (zoom >= 6) size = 8;
        else size = 10;

        int minDistance;
        if (zoom >= 8) minDistance = 42;
        else if (zoom >= 7) minDistance = 30;
        else if (zoom >= 6) minDistance = 20;
        else minDistance = 0;

        List<Point2D> kept = new ArrayList<>();
        int lastIndex = points.size() - 1;
        for (int i = 0; i < points.size(); i++) {
            Point2D pt = points.get(i);
            boolean forceRender = (i == 0 || i == lastIndex);

            if (!forceRender && minDistance > 0 && isTooClose(pt, kept, minDistance)) {
                continue;
            }

            int x = (int) pt.getX() - size / 2;
            int y = (int) pt.getY() - size / 2;

            g.setColor(Color.WHITE);
            g.fillOval(x, y, size, size);
            g.setColor(outlineColor);
            g.setStroke(new BasicStroke(2));
            g.drawOval(x, y, size, size);

            if (minDistance > 0) {
                kept.add(pt);
            }
        }
    }

    private boolean isTooClose(Point2D point, List<Point2D> kept, int minDistance) {
        int minDistance2 = minDistance * minDistance;
        for (Point2D k : kept) {
            double dx = point.getX() - k.getX();
            double dy = point.getY() - k.getY();
            if ((dx * dx + dy * dy) < minDistance2) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRoute() {
        return route != null && route.size() >= 2;
    }
}


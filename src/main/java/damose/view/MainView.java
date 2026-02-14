package damose.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import damose.config.AppConstants;
import damose.model.Stop;
import damose.view.component.ConnectionButton;
import damose.view.component.FloatingArrivalPanel;
import damose.view.component.RouteSidePanel;
import damose.view.component.SearchOverlay;
import damose.view.component.ServiceQualityPanel;
import damose.view.map.GeoUtils;
import damose.view.map.MapFactory;

/**
 * Main application view - Midnight Dark style.
 */
public class MainView {

    private JFrame frame;
    private JXMapViewer mapViewer;
    private JButton searchButton;
    private JButton favoritesButton;
    private JButton closeButton;
    private JButton maxButton;
    private JButton minButton;
    private JButton busToggleButton;
    private JPanel mapControlsPanel;
    private ConnectionButton connectionButton;
    private SearchOverlay searchOverlay;
    private JPanel overlayPanel;
    private FloatingArrivalPanel floatingPanel;
    private RouteSidePanel routeSidePanel;
    private GeoPosition floatingAnchorGeo;
    private ServiceQualityPanel serviceQualityPanel;
    private List<Stop> allStopsCache = new ArrayList<>();
    private List<Stop> allLinesCache = new ArrayList<>();

    private Point dragOffset;
    private boolean isDragging = false;
    private Rectangle normalBounds = new Rectangle(100, 100, 1100, 750); // Store normal window bounds
    private static final int LEFT_STACK_X = 10;
    private static final int LEFT_STACK_Y = 10;
    private static final int MAP_CONTROLS_WIDTH = 58;
    private static final int MAP_CONTROLS_HEIGHT = 225;
    private static final int ROUTE_PANEL_WIDTH = 340;
    private static final int ROUTE_PANEL_TOP = 48;
    private static final int ROUTE_PANEL_MARGIN = 12;
    private static final int WINDOW_CONTROL_SIZE = 34;
    private static final int WINDOW_CONTROL_TOP = 6;
    private static final int WINDOW_CONTROL_RIGHT_MARGIN = 6;
    private static final int WINDOW_CONTROL_GAP = 2;
    private Runnable onFloatingPanelClose;
    private Runnable onRoutePanelClose;
    private IntConsumer onRouteDirectionSelected;

    private final PropertyChangeListener mapListener = evt -> {
        String name = evt.getPropertyName();
        if ("zoom".equals(name) || "center".equals(name) || "tileFactory".equals(name)) {
            updateFloatingPanelPosition();
        }
    };

    public void showSearchOverlay() {
        if (searchOverlay != null) searchOverlay.showSearch();
    }

    public void setSearchData(List<Stop> stops, List<Stop> lines) {
        this.allLinesCache = lines != null ? lines : new ArrayList<>();
        if (searchOverlay != null) {
            searchOverlay.setData(stops, lines);
        }
    }

    public void setOnSearchSelect(java.util.function.Consumer<Stop> callback) {
        if (searchOverlay != null) {
            searchOverlay.setOnSelect(callback);
        }
    }

    public JButton getSearchButton() {
        return searchButton;
    }
    
    public JButton getFavoritesButton() {
        return favoritesButton;
    }
    
    public JButton getBusToggleButton() {
        return busToggleButton;
    }
    
    public ConnectionButton getConnectionButton() {
        return connectionButton;
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

    public void setFloatingPanelMaxRows(int maxRows) {
        if (floatingPanel != null) {
            floatingPanel.setPreferredRowsMax(maxRows);
        }
    }

    public void init() {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception ignored) {
        }

        UIManager.put("Button.arc", 20);
        UIManager.put("TextField.arc", 15);

        frame = new JFrame("Damose");
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null);
        
        // Set rounded corners
        frame.setShape(new RoundRectangle2D.Double(0, 0, 1100, 750, 20, 20));
        
        // Update shape on resize
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    frame.setShape(null);
                } else {
                    frame.setShape(new RoundRectangle2D.Double(0, 0, 
                        frame.getWidth(), frame.getHeight(), 20, 20));
                }
            }
        });
        
        // Set app icon
        try {
            Image trimmedIcon = loadTrimmedImage("/sprites/icon.png");
            if (trimmedIcon != null) {
                List<Image> icons = new ArrayList<>();
                icons.add(trimmedIcon.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
                icons.add(trimmedIcon.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
                icons.add(trimmedIcon.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                icons.add(trimmedIcon.getScaledInstance(48, 48, Image.SCALE_SMOOTH));
                icons.add(trimmedIcon.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
                icons.add(trimmedIcon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                frame.setIconImages(icons);
            }
        } catch (Exception e) {
            System.out.println("Could not load app icon: " + e.getMessage());
        }

        // Map takes the whole window
        mapViewer = MapFactory.createMapViewer();

        JLayeredPane layeredPane = new JLayeredPane();
        frame.setContentPane(layeredPane);

        mapViewer.setBounds(0, 0, 1100, 750);
        layeredPane.add(mapViewer, JLayeredPane.DEFAULT_LAYER);

        overlayPanel = new JPanel(null);
        overlayPanel.setOpaque(false);
        overlayPanel.setBounds(0, 0, 1100, 750);
        layeredPane.add(overlayPanel, JLayeredPane.PALETTE_LAYER);
        
        mapControlsPanel = createMapControlsPanel();
        mapControlsPanel.setBounds(LEFT_STACK_X, LEFT_STACK_Y, MAP_CONTROLS_WIDTH, MAP_CONTROLS_HEIGHT);
        overlayPanel.add(mapControlsPanel);

        // Search button (top-left)
        ImageIcon lensIcon = new ImageIcon(getClass().getResource("/sprites/lente.png"));
        Image scaledLens = lensIcon.getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
        searchButton = new JButton(new ImageIcon(scaledLens));
        searchButton.setContentAreaFilled(false);
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setBounds(5, 5, 48, 48);
        searchButton.setToolTipText("Cerca fermate e linee");
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mapControlsPanel.add(searchButton);
        
        // Favorites button (below search button)
        ImageIcon starIcon = new ImageIcon(getClass().getResource("/sprites/star.png"));
        Image scaledStar = starIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        favoritesButton = new JButton(new ImageIcon(scaledStar));
        favoritesButton.setContentAreaFilled(false);
        favoritesButton.setBorderPainted(false);
        favoritesButton.setFocusPainted(false);
        favoritesButton.setBounds(5, 60, 48, 48);
        favoritesButton.setToolTipText("Fermate preferite");
        favoritesButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mapControlsPanel.add(favoritesButton);
        
        // Bus visibility toggle button (below favorites)
        ImageIcon busIcon = new ImageIcon(getClass().getResource("/sprites/bus1.png"));
        Image scaledBus = busIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        busToggleButton = new JButton(new ImageIcon(scaledBus));
        busToggleButton.setContentAreaFilled(false);
        busToggleButton.setBorderPainted(false);
        busToggleButton.setFocusPainted(false);
        busToggleButton.setBounds(5, 115, 48, 48);
        busToggleButton.setToolTipText("Mostra/Nascondi autobus");
        busToggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mapControlsPanel.add(busToggleButton);
        
        // Window control buttons (top-right corner) - add first so connection button is below
        createWindowControls();
        
        // Connection button (in the same top-left controls panel)
        connectionButton = new ConnectionButton();
        connectionButton.setBounds((MAP_CONTROLS_WIDTH - ConnectionButton.BUTTON_WIDTH) / 2,
                170,
                ConnectionButton.BUTTON_WIDTH,
                ConnectionButton.BUTTON_HEIGHT);
        mapControlsPanel.add(connectionButton);
        
        // Service quality panel (top-left, next to connection card)
        serviceQualityPanel = new ServiceQualityPanel();
        serviceQualityPanel.setBounds(15, 750 - 65, 180, 50);
        overlayPanel.add(serviceQualityPanel);

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = layeredPane.getWidth();
                int h = layeredPane.getHeight();
                mapViewer.setBounds(0, 0, w, h);
                overlayPanel.setBounds(0, 0, w, h);
                if (searchOverlay != null) {
                    searchOverlay.setBounds(0, 0, w, h);
                }
                // Update button positions
                updateWindowControlPositions(w);
                if (serviceQualityPanel != null) {
                    serviceQualityPanel.setBounds(15, h - 65, 180, 50);
                }
                if (routeSidePanel != null) {
                    routeSidePanel.setBounds(w - ROUTE_PANEL_WIDTH - ROUTE_PANEL_MARGIN, ROUTE_PANEL_TOP,
                            ROUTE_PANEL_WIDTH, h - ROUTE_PANEL_TOP - ROUTE_PANEL_MARGIN);
                }
                updateFloatingPanelPosition();
            }
        });
        
        // Enable window dragging from map
        enableWindowDrag();

        floatingPanel = new FloatingArrivalPanel();
        floatingPanel.setVisible(false);
        floatingPanel.setOnClose(() -> {
            floatingAnchorGeo = null;
            if (onFloatingPanelClose != null) {
                onFloatingPanelClose.run();
            }
        });
        overlayPanel.add(floatingPanel);

        searchOverlay = new SearchOverlay();
        searchOverlay.setVisible(false);
        searchOverlay.setBounds(0, 0, 1100, 750);
        layeredPane.add(searchOverlay, JLayeredPane.POPUP_LAYER);
        
        routeSidePanel = new RouteSidePanel();
        routeSidePanel.setVisible(false);
        routeSidePanel.setBounds(1100 - ROUTE_PANEL_WIDTH - ROUTE_PANEL_MARGIN, ROUTE_PANEL_TOP,
                ROUTE_PANEL_WIDTH, 750 - ROUTE_PANEL_TOP - ROUTE_PANEL_MARGIN);
        routeSidePanel.setOnClose(() -> {
            if (onRoutePanelClose != null) {
                onRoutePanelClose.run();
            }
        });
        routeSidePanel.setOnDirectionSelected(directionId -> {
            if (onRouteDirectionSelected != null) {
                onRouteDirectionSelected.accept(directionId);
            }
        });
        overlayPanel.add(routeSidePanel);

        mapViewer.addPropertyChangeListener(mapListener);
        setFloatingPanelMaxRows(10);

        frame.setVisible(true);
    }
    
    private void createWindowControls() {
        // Close button (top-right corner)
        closeButton = createWindowControlButton(WindowControlType.CLOSE);
        closeButton.setBounds(1100 - 40, WINDOW_CONTROL_TOP, WINDOW_CONTROL_SIZE, WINDOW_CONTROL_SIZE);
        closeButton.addActionListener(e -> System.exit(0));
        overlayPanel.add(closeButton);
        
        // Maximize button
        maxButton = createWindowControlButton(WindowControlType.MAXIMIZE);
        maxButton.setBounds(1100 - 76, WINDOW_CONTROL_TOP, WINDOW_CONTROL_SIZE, WINDOW_CONTROL_SIZE);
        maxButton.addActionListener(e -> {
            if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                // Restore to normal size
                frame.setExtendedState(JFrame.NORMAL);
                frame.setBounds(normalBounds);
            } else {
                // Save current bounds before maximizing
                normalBounds = frame.getBounds();
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });
        overlayPanel.add(maxButton);
        
        // Minimize button
        minButton = createWindowControlButton(WindowControlType.MINIMIZE);
        minButton.setBounds(1100 - 112, WINDOW_CONTROL_TOP, WINDOW_CONTROL_SIZE, WINDOW_CONTROL_SIZE);
        minButton.addActionListener(e -> frame.setState(JFrame.ICONIFIED));
        overlayPanel.add(minButton);
    }

    private JPanel createMapControlsPanel() {
        JPanel panel = createOverlayCardPanel();
        panel.setLayout(null);
        return panel;
    }

    private JPanel createOverlayCardPanel() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(AppConstants.OVERLAY_CARD_BG);
                g2.fillRoundRect(0, 0, w, h, AppConstants.OVERLAY_CARD_ARC, AppConstants.OVERLAY_CARD_ARC);

                g2.setColor(AppConstants.OVERLAY_CARD_BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, AppConstants.OVERLAY_CARD_ARC, AppConstants.OVERLAY_CARD_ARC);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }
    
    private enum WindowControlType {
        CLOSE,
        MAXIMIZE,
        MINIMIZE
    }

    private JButton createWindowControlButton(WindowControlType type) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                boolean hovered = getModel().isRollover();

                int w = getWidth();
                int h = getHeight();
                int d = Math.min(w, h) - 6;
                int x = (w - d) / 2;
                int y = (h - d) / 2;

                Color fill = hovered
                        ? (type == WindowControlType.CLOSE ? new Color(235, 85, 85, 230) : new Color(125, 125, 140, 220))
                        : new Color(28, 28, 34, 200);
                Color border = hovered
                        ? (type == WindowControlType.CLOSE ? new Color(255, 140, 140, 220) : new Color(165, 165, 180, 210))
                        : new Color(80, 80, 94, 170);
                Color glyph = hovered && type != WindowControlType.CLOSE
                        ? new Color(250, 250, 255)
                        : new Color(228, 228, 236);

                g2.setColor(fill);
                g2.fillOval(x, y, d, d);
                g2.setColor(border);
                g2.drawOval(x, y, d, d);

                int cx = w / 2;
                int cy = h / 2;
                g2.setColor(glyph);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                switch (type) {
                    case CLOSE -> {
                        g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                        g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
                    }
                    case MAXIMIZE -> g2.drawRect(cx - 5, cy - 5, 10, 10);
                    case MINIMIZE -> g2.drawLine(cx - 6, cy + 3, cx + 6, cy + 3);
                }
                g2.dispose();
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        btn.setRolloverEnabled(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.repaint();
            }
        });
        return btn;
    }
    
    private void updateWindowControlPositions(int width) {
        int closeX = width - WINDOW_CONTROL_RIGHT_MARGIN - WINDOW_CONTROL_SIZE;
        int maxX = closeX - WINDOW_CONTROL_SIZE - WINDOW_CONTROL_GAP;
        int minX = maxX - WINDOW_CONTROL_SIZE - WINDOW_CONTROL_GAP;
        if (closeButton != null) closeButton.setBounds(closeX, WINDOW_CONTROL_TOP, WINDOW_CONTROL_SIZE, WINDOW_CONTROL_SIZE);
        if (maxButton != null) maxButton.setBounds(maxX, WINDOW_CONTROL_TOP, WINDOW_CONTROL_SIZE, WINDOW_CONTROL_SIZE);
        if (minButton != null) minButton.setBounds(minX, WINDOW_CONTROL_TOP, WINDOW_CONTROL_SIZE, WINDOW_CONTROL_SIZE);
    }

    private Image loadTrimmedImage(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            return null;
        }

        ImageIcon rawIcon = new ImageIcon(url);
        if (rawIcon.getIconWidth() <= 0 || rawIcon.getIconHeight() <= 0) {
            return null;
        }

        BufferedImage source = new BufferedImage(rawIcon.getIconWidth(), rawIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = source.createGraphics();
        g2.drawImage(rawIcon.getImage(), 0, 0, null);
        g2.dispose();

        return trimTransparentBorders(source);
    }

    private BufferedImage trimTransparentBorders(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        int minX = w;
        int minY = h;
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int alpha = (source.getRGB(x, y) >>> 24) & 0xFF;
                if (alpha > 8) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return source;
        }

        return source.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
    
    private void enableWindowDrag() {
        // Allow dragging window from empty areas at the top
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Only allow drag from top 50px area
                if (e.getY() < 50 && e.getX() < mapViewer.getWidth() - 120) {
                    dragOffset = e.getPoint();
                    isDragging = true;
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }
        });
        
        mapViewer.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && dragOffset != null) {
                    Point loc = frame.getLocation();
                    frame.setLocation(loc.x + e.getX() - dragOffset.x, loc.y + e.getY() - dragOffset.y);
                }
            }
        });
    }

    public void addWaypointClickListener() {
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (allStopsCache.isEmpty()) return;

                int x = e.getX();
                int y = e.getY();

                GeoPosition clickedPos = mapViewer.convertPointToGeoPosition(e.getPoint());
                Stop nearest = findNearestStop(clickedPos);

                if (nearest != null && GeoUtils.isClickCloseToStop(mapViewer, nearest, x, y)) {
                    if (stopClickListener != null) {
                        stopClickListener.onStopClicked(nearest);
                    }
                }
            }
        });
    }

    private Stop findNearestStop(GeoPosition pos) {
        double minDist = Double.MAX_VALUE;
        Stop nearest = null;

        for (Stop s : allStopsCache) {
            double d = GeoUtils.haversine(
                    pos.getLatitude(), pos.getLongitude(),
                    s.getStopLat(), s.getStopLon()
            );
            if (d < minDist) {
                minDist = d;
                nearest = s;
            }
        }
        return nearest;
    }

    public interface StopClickListener {
        void onStopClicked(Stop stop);
    }

    private StopClickListener stopClickListener;

    public void setStopClickListener(StopClickListener listener) {
        this.stopClickListener = listener;
    }

    public void setAllStops(List<Stop> stops) {
        this.allStopsCache = stops;
    }

    public void showFloatingPanel(String stopName, String stopId, List<String> arrivi, 
                                   boolean isFavorite, Point2D pos, GeoPosition anchorGeo) {
        floatingPanel.update(stopName, stopId, arrivi, isFavorite);
        this.floatingAnchorGeo = anchorGeo;

        Point2D p = pos;
        if (p == null && anchorGeo != null) {
            p = mapViewer.convertGeoPositionToPoint(anchorGeo);
        }

        if (p != null) {
            Dimension pref = floatingPanel.getPreferredPanelSize();
            int panelWidth = pref.width;
            int panelHeight = pref.height;
            int x = (int) p.getX() - panelWidth / 2;
            int y = (int) p.getY() - panelHeight - 8;

            int maxX = Math.max(10, mapViewer.getWidth() - panelWidth - 10);
            int maxY = Math.max(10, mapViewer.getHeight() - panelHeight - 10);
            x = Math.max(10, Math.min(x, maxX));
            y = Math.max(10, Math.min(y, maxY));

            floatingPanel.setBounds(x, y, panelWidth, panelHeight);
        }

        floatingPanel.revalidate();
        floatingPanel.repaint();
        floatingPanel.fadeIn(300, 15);
    }

    public void showFloatingPanel(String stopName, List<String> arrivi, Point2D pos) {
        showFloatingPanel(stopName, null, arrivi, false, pos, null);
    }
    
    public String getFloatingPanelStopId() {
        return floatingPanel.getCurrentStopId();
    }
    
    public boolean isFloatingPanelVisible() {
        return floatingPanel.isVisible();
    }
    
    public void refreshFloatingPanel(String stopName, String stopId, List<String> arrivi, boolean isFavorite) {
        floatingPanel.update(stopName, stopId, arrivi, isFavorite);
        floatingPanel.repaint();
    }
    
    public void updateFloatingPanelFavorite(boolean isFavorite) {
        floatingPanel.setFavoriteStatus(isFavorite);
    }
    
    public void setOnFavoriteToggle(Runnable callback) {
        floatingPanel.setOnFavoriteToggle(callback);
    }
    
    public void setOnViewAllTrips(Runnable callback) {
        floatingPanel.setOnViewAllTrips(callback);
    }

    public void setOnFloatingPanelClose(Runnable callback) {
        this.onFloatingPanelClose = callback;
    }

    public void setOnRoutePanelClose(Runnable callback) {
        this.onRoutePanelClose = callback;
    }

    public void setOnRouteDirectionSelected(IntConsumer callback) {
        this.onRouteDirectionSelected = callback;
    }
    
    public void showAllTripsInPanel(List<String> allTrips) {
        floatingPanel.showAllTripsView(allTrips);
    }
    
    public void showFavoritesInSearch(List<Stop> favorites) {
        searchOverlay.showFavorites(favorites);
    }

    public void hideFloatingPanel() {
        floatingPanel.setVisible(false);
        floatingAnchorGeo = null;
    }

    public void showRouteSidePanel(String routeName, List<Stop> routeStops) {
        if (routeSidePanel == null) return;
        routeSidePanel.setRoute(routeName, routeStops);
        routeSidePanel.setVisible(true);
        routeSidePanel.repaint();
    }

    public void setRouteSidePanelDirections(Map<Integer, String> directions, int selectedDirection) {
        if (routeSidePanel == null) return;
        routeSidePanel.setDirectionOptions(directions, selectedDirection);
    }

    public void updateRouteSidePanelVehicles(List<RouteSidePanel.VehicleMarker> markers) {
        if (routeSidePanel == null || !routeSidePanel.isVisible()) return;
        routeSidePanel.setVehicleMarkers(markers);
    }

    public void hideRouteSidePanel() {
        if (routeSidePanel == null) return;
        routeSidePanel.setVisible(false);
    }

    private void updateFloatingPanelPosition() {
        if (floatingAnchorGeo == null) return;

        // STEP 1: PROJECT - Convert world coordinates to screen coordinates
        Point2D projectedPos = mapViewer.convertGeoPositionToPoint(floatingAnchorGeo);
        
        // STEP 2: VALIDATE PROJECTION - If projection fails, stop is off-screen
        if (projectedPos == null) {
            if (floatingPanel.isVisible()) {
                floatingPanel.setVisible(false);
            }
            return;
        }

        int mapWidth = mapViewer.getWidth();
        int mapHeight = mapViewer.getHeight();
        double stopX = projectedPos.getX();
        double stopY = projectedPos.getY();

        // STEP 3: DETECT OUT-OF-BOUNDS - Check if stop projection is beyond viewport
        // If stop is beyond reasonable margin, hide panel (it's truly off-screen)
        int offScreenMargin = 200;
        if (stopX < -offScreenMargin || stopX > mapWidth + offScreenMargin ||
            stopY < -offScreenMargin || stopY > mapHeight + offScreenMargin) {
            if (floatingPanel.isVisible()) {
                floatingPanel.setVisible(false);
            }
            return;
        }

        // STEP 4: CALCULATE TARGET POSITION - Where panel should go based on stop projection
        Dimension pref = floatingPanel.getPreferredPanelSize();
        int panelWidth = pref.width;
        int panelHeight = pref.height;
        
        // Calculate ideal panel position (centered above stop, no clamping)
        int targetX = (int) stopX - panelWidth / 2;
        int targetY = (int) stopY - panelHeight - 8;

        // STEP 5: DETACHMENT VALIDATION - Check if panel can be placed without clamping
        // If the panel needs to be clamped to screen bounds, it means the stop is at the edge
        // and the panel position would diverge from the stop's true projection.
        // This is detachment - the panel would "stick" to screen edge while stop goes off-screen.
        
        int minValidX = 5;
        int maxValidX = mapWidth - panelWidth - 5;
        int minValidY = 5;
        int maxValidY = mapHeight - panelHeight - 5;
        
        // Panel cannot be placed at its true position without clamping
        if (targetX < minValidX || targetX > maxValidX ||
            targetY < minValidY || targetY > maxValidY) {
            // Stop is too close to edge - panel would need clamping
            // This breaks the spatial binding, so hide the panel
            if (floatingPanel.isVisible()) {
                floatingPanel.setVisible(false);
            }
            return;
        }

        // STEP 6: SHOW AND POSITION - Panel can stay bound to stop projection
        // Stop is visible and panel can be placed at its true screen position
        if (!floatingPanel.isVisible()) {
            floatingPanel.setVisible(true);
        }

        // Place panel at exact projected position (no clamping or correction)
        floatingPanel.setBounds(targetX, targetY, panelWidth, panelHeight);
        floatingPanel.revalidate();
        floatingPanel.repaint();
    }
}


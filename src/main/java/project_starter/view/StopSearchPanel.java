package project_starter.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import project_starter.datas.Stops;

public class StopSearchPanel extends JPanel { // Pannello laterale di ricerca fermate/linee

    private JTextField searchField;
    private DefaultListModel<Stops> stopListModel;
    private JList<Stops> stopList;
    private JPanel stopDetailsPanel;

    private JRadioButton stopsMode;
    private JRadioButton linesMode;

    public StopSearchPanel() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(280, 700));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        setBackground(new Color(40, 40, 40));
        setVisible(false);

        // --- Pannello superiore con campo ricerca + toggle ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(40, 40, 40));

        searchField = new JTextField();
        topPanel.add(searchField, BorderLayout.CENTER);

        stopsMode = new JRadioButton("Fermate", true);
        linesMode = new JRadioButton("Linee");
        stopsMode.setBackground(new Color(40, 40, 40));
        stopsMode.setForeground(Color.WHITE);
        linesMode.setBackground(new Color(40, 40, 40));
        linesMode.setForeground(Color.WHITE);

        ButtonGroup group = new ButtonGroup();
        group.add(stopsMode);
        group.add(linesMode);

        JPanel modePanel = new JPanel();
        modePanel.setBackground(new Color(40, 40, 40));
        modePanel.add(stopsMode);
        modePanel.add(linesMode);

        topPanel.add(modePanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // --- Lista fermate/linee ---
        stopListModel = new DefaultListModel<>();
        stopList = new JList<>(stopListModel);
        stopList.setBackground(new Color(60, 60, 60));
        stopList.setForeground(Color.WHITE);
        add(new JScrollPane(stopList), BorderLayout.CENTER);

        // --- Dettagli fermata/linea ---
        stopDetailsPanel = new JPanel(new GridLayout(0, 1));
        stopDetailsPanel.setBorder(BorderFactory.createTitledBorder("Dettagli"));
        stopDetailsPanel.setBackground(new Color(50, 50, 50));
        add(stopDetailsPanel, BorderLayout.SOUTH);
    }

    // -------- Accessori --------
    public JTextField getSearchField() { return searchField; }
    public JList<Stops> getStopList() { return stopList; }

    public void clearStopList() { stopListModel.clear(); }
    public void addStopToList(Stops stop) { stopListModel.addElement(stop); }

    public void setStopInfo(Stops stop) {
        stopDetailsPanel.removeAll();

        // 🔎 Se è una linea fittizia → mostra solo il nome
        if (stop.isFakeLine()) {
            stopDetailsPanel.add(new JLabel("Linea: " + stop.getStopName()));
        } else {
            // 🔎 Fermata reale → mostra ID, nome e coordinate
            stopDetailsPanel.add(new JLabel("ID: " + stop.getStopId()));
            stopDetailsPanel.add(new JLabel("Nome: " + stop.getStopName()));
            stopDetailsPanel.add(new JLabel("Lat: " + stop.getStopLat()));
            stopDetailsPanel.add(new JLabel("Lon: " + stop.getStopLon()));
        }

        stopDetailsPanel.revalidate();
        stopDetailsPanel.repaint();
    }

    public boolean isStopsMode() { return stopsMode.isSelected(); }
    public boolean isLinesMode() { return linesMode.isSelected(); }
}

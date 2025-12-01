package project_starter.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import project_starter.datas.Stops;

public class StopInfoDialog extends JDialog { // Dialog di informazioni fermata

    public StopInfoDialog(Stops stop) {
        setTitle("Dettagli Fermata");
        setSize(300, 250);
        setLocationRelativeTo(null);
        setModal(true);

        JPanel panel = new JPanel(new GridLayout(0, 1));

        panel.add(new JLabel("ID: " + stop.getStopId()));
        panel.add(new JLabel("Codice: " + stop.getStopCode()));
        panel.add(new JLabel("Nome: " + stop.getStopName()));
        panel.add(new JLabel("Lat: " + stop.getStopLat()));
        panel.add(new JLabel("Lon: " + stop.getStopLon()));

        add(panel, BorderLayout.CENTER);
    }
}

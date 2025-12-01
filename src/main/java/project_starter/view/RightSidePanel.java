package project_starter.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class RightSidePanel extends JPanel {

    private JLabel title;
    private JPanel arrivalsList;
    private JButton closeButton;

    public RightSidePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 0)); // larghezza fissa
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));

        // Header con titolo + bottone chiusura
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(230, 230, 230));
        header.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        title = new JLabel("Arrivi");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        closeButton = new JButton("✖");
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        closeButton.setForeground(Color.DARK_GRAY);

        // Azione chiusura
        closeButton.addActionListener(e -> setVisible(false));

        header.add(title, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Lista arrivi
        arrivalsList = new JPanel();
        arrivalsList.setLayout(new BoxLayout(arrivalsList, BoxLayout.Y_AXIS));
        arrivalsList.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(arrivalsList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        setVisible(false);
    }

    /** Aggiorna il pannello con nuovi arrivi */
    public void update(String stopName, List<String> arrivi) {
        title.setText("Arrivi a " + stopName);

        arrivalsList.removeAll();

        for (String a : arrivi) {
            JLabel label = new JLabel("• " + a);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            arrivalsList.add(label);
        }

        arrivalsList.revalidate();
        arrivalsList.repaint();

        setVisible(true);
    }
}

package project_starter;
import javax.swing.SwingUtilities;

import project_starter.controller.RealTimeBusTrackerController;


public class RealTimeBusTrackerApp {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            RealTimeBusTrackerController controller = new RealTimeBusTrackerController();
            controller.start();
        });
    }
}

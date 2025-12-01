package project_starter.view;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import project_starter.datas.Stops;


public class StopSearchDialog extends JDialog { // Dialog di ricerca fermate

    private JTextField searchField;
    private DefaultListModel<Stops> listModel;
    private JList<Stops> resultList;

    public StopSearchDialog(List<Stops> fermate, Consumer<Stops> onStopSelected) {
        setTitle("Cerca Fermata");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setModal(true);

        searchField = new JTextField();
        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);

        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // aggiorna risultati mentre scrivi
        searchField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            String q = searchField.getText().toLowerCase();
            listModel.clear();

            fermate.stream()
                .filter(s -> s.getStopName().toLowerCase().contains(q))
                .forEach(listModel::addElement);
        });

        // doppio click → seleziona fermata
        resultList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && resultList.getSelectedValue() != null) {
                Stops s = resultList.getSelectedValue();
                onStopSelected.accept(s);
                dispose();
            }
        });

        add(searchField, BorderLayout.NORTH);
        add(new JScrollPane(resultList), BorderLayout.CENTER);
    }

    // utility
    public interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent e);

        @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(e); }
    }
}

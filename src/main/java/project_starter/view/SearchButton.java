package project_starter.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import project_starter.datas.Stops;


public class SearchButton extends JButton { // Pulsante di ricerca fermate

    public SearchButton(Image iconImage, Supplier<List<Stops>> fermateSupplier, Consumer<Stops> onStopSelected) {
        super(new ImageIcon(iconImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
        setPreferredSize(new Dimension(40, 40));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);

        addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            JPanel content = new JPanel(new BorderLayout(8, 8));

            JTextField queryField = new JTextField();
            DefaultListModel<Stops> model = new DefaultListModel<>();
            JList<Stops> resultsList = new JList<>(model);

            resultsList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Stops s) setText(s.getStopName());
                    return this;
                }
            });

            JScrollPane scroll = new JScrollPane(resultsList);
            content.add(queryField, BorderLayout.NORTH);
            content.add(scroll, BorderLayout.CENTER);

            Runnable refresh = () -> {
                String q = queryField.getText().trim().toLowerCase();
                model.clear();
                fermateSupplier.get().stream()
                        .filter(s -> s.getStopName() != null && s.getStopName().toLowerCase().contains(q))
                        .limit(100)
                        .forEach(model::addElement);
            };
            refresh.run();

            queryField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh.run(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh.run(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh.run(); }
            });

            resultsList.addListSelectionListener(ev -> {
                if (!ev.getValueIsAdjusting()) {
                    Stops selected = resultsList.getSelectedValue();
                    if (selected != null) {
                        onStopSelected.accept(selected);
                        menu.setVisible(false);
                    }
                }
            });

            menu.add(content);
            menu.show(this, 0, getHeight());
            SwingUtilities.invokeLater(queryField::requestFocusInWindow);
        });
    }
}

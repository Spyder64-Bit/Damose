package project_starter;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SimpleDocumentListener implements DocumentListener {

    private final Runnable callback;

    public SimpleDocumentListener(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void insertUpdate(DocumentEvent e) { callback.run(); }

    @Override
    public void removeUpdate(DocumentEvent e) { callback.run(); }

    @Override
    public void changedUpdate(DocumentEvent e) { callback.run(); }
}

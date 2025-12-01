package project_starter;
import javax.swing.JButton;

class IconButton extends JButton {
    private java.awt.Image background;

    public IconButton(java.awt.Image img) {
        this.background = img;
        setContentAreaFilled(false); // niente sfondo Swing
        setBorderPainted(false);     // niente bordo
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

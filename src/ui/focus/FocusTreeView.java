package ui.focus;

import javax.swing.*;
import java.awt.*;

public class FocusTreeView extends JPanel {
    public FocusTreeView() {

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.drawRect(25, 25, 100, 100);
        g.drawString("No.", 50, 50);
    }
}

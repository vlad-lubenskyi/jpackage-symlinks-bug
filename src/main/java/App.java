import javax.swing.*;

import static javax.swing.SwingUtilities.invokeLater;

public final class App {

    public static void main(String[] args) {

        invokeLater(() -> {
            var frame = new JFrame("Hello World Swing");
            frame.add(new JButton("This button does nothing"));
            frame.setSize(1280, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

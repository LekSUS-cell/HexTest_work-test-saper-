package HexGame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hexcells Infinite");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(800, 600));

            CardLayout cardLayout = new CardLayout();
            JPanel container = new JPanel(cardLayout);
            container.setPreferredSize(new Dimension(800, 600));

            MainMenu mainMenu = new MainMenu(frame, container, cardLayout);
            mainMenu.setName("MainMenu");
            container.add(mainMenu, "MainMenu");

            frame.add(container);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            cardLayout.show(container, "MainMenu");
        });
    }
}
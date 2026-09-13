package src.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import src.managers.WorldManager;

public class MainMenuPanel extends JPanel {
    private final Runnable newGameAction;
    private final WorldManager config;

    public MainMenuPanel(Runnable newGameAction, WorldManager config) {
        this.newGameAction = newGameAction;
        this.config = config;
        setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        showMenu();
    }

    private void showMenu() {
        removeAll();
        setLayout(new BorderLayout(16, 16));
        JLabel title = new JLabel("farm game", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        add(title, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(2, 1, 8, 8));
        JButton newGameButton = new JButton("new game");
        JButton settingsButton = new JButton("settings");
        buttons.add(newGameButton);
        buttons.add(settingsButton);
        add(buttons, BorderLayout.CENTER);
        newGameButton.addActionListener(event -> newGameAction.run());
        settingsButton.addActionListener(event -> showSettings());
    }

    private void showSettings() {
        JTextArea settings = new JTextArea();
        settings.setEditable(false);
        settings.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        StringBuilder text = new StringBuilder("world generation settings\n\n");
        for (Map.Entry<String, Double> entry : config.values().entrySet()) {
            text.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
        settings.setText(text.toString());

        JButton backButton = new JButton("back");
        backButton.addActionListener(event -> {
            showMenu();
            revalidate();
            repaint();
        });
        removeAll();
        setLayout(new BorderLayout(16, 16));
        add(new JScrollPane(settings), BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

}

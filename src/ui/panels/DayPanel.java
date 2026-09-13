package src.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DayPanel extends JPanel {
    private final JLabel dayLabel = new JLabel("day 1");
    private final JLabel timerLabel = new JLabel("05:00");
    private final JButton pauseButton = new JButton("pause");

    public DayPanel(Runnable pauseAction, int width) {
        setBorder(BorderFactory.createTitledBorder("time"));
        setLayout(new BorderLayout(6, 6));
        setPreferredSize(new Dimension(width, 82));

        JPanel status = new JPanel(new GridLayout(2, 1));
        status.add(dayLabel);
        status.add(timerLabel);
        add(status, BorderLayout.CENTER);

        pauseButton.addActionListener(action -> pauseAction.run());
        add(pauseButton, BorderLayout.EAST);
    }

    public void updateTime(int day, long remainingSeconds) {
        dayLabel.setText("day " + day);
        timerLabel.setText(String.format("%02d:%02d", remainingSeconds / 60,
                remainingSeconds % 60));
    }

    public void setPaused(boolean paused) {
        pauseButton.setText(paused ? "play" : "pause");
    }
}

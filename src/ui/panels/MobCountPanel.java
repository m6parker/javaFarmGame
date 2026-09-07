package src.ui.panels;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import src.entities.Chicken;
import src.entities.Dweller;
import src.entities.Fish;
import src.entities.Mob;
import src.entities.Sheep;

public class MobCountPanel extends JPanel {
    private static final String[] MOB_NAMES = {"fish", "sheep", "chicken", "dweller"};
    private final List<Mob> mobs;
    private final JLabel[] countLabels = new JLabel[MOB_NAMES.length];

    public MobCountPanel(List<Mob> mobs, int screenHeight) {
        this.mobs = mobs;
        setBorder(BorderFactory.createTitledBorder("mobs"));
        setLayout(new GridLayout(0, 1, 4, 4));
        setPreferredSize(new Dimension(180, screenHeight));
        createRows();
    }

    public void refreshCounts() {
        int[] counts = new int[MOB_NAMES.length];
        for (Mob mob : mobs) {
            if (mob instanceof Fish) {
                counts[0]++;
            } else if (mob instanceof Sheep) {
                counts[1]++;
            } else if (mob instanceof Chicken) {
                counts[2]++;
            } else if (mob instanceof Dweller) {
                counts[3]++;
            }
        }
        for (int i = 0; i < countLabels.length; i++) {
            countLabels[i].setText(MOB_NAMES[i] + ": " + counts[i]);
        }
    }

    private void createRows() {
        for (int i = 0; i < MOB_NAMES.length; i++) {
            countLabels[i] = new JLabel();
            add(countLabels[i]);
        }
        refreshCounts();
    }
}

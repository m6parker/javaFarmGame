package src.ui.panels;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import src.managers.BuildingManager;

public class BuildingCountPanel extends JPanel implements BuildingManager.CountListener {
    private final BuildingManager buildingManager;
    private final JLabel[] countLabels = new JLabel[BuildingManager.BUILDING_COUNT];

    // constructor
    public BuildingCountPanel(BuildingManager buildingManager, int screenHeight) {
        this.buildingManager = buildingManager;
        setBorder(BorderFactory.createTitledBorder("buildings"));
        setLayout(new GridLayout(0, 1, 4, 4));
        setPreferredSize(new Dimension(180, screenHeight));
        createRows();
        buildingManager.addCountListener(this);
    }

    @Override
    public void countsChanged() {
        for (int i = 0; i < countLabels.length; i++) {
            countLabels[i].setText(buildingManager.getBuildingName(i) + ": "
                    + buildingManager.getBuildingCount(i));
        }
    }

    // create rows for each building type with thumbnail and count
    private void createRows() {
        for (int i = 0; i < BuildingManager.BUILDING_COUNT; i++) {
            JPanel buildingRow = new JPanel(new BorderLayout(6, 0));
            BufferedImage building = buildingManager.getBuildingImage(i);
            if (building != null) {
                Image thumbnail = building.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                buildingRow.add(new JLabel(new ImageIcon(thumbnail)), BorderLayout.WEST);
            }

            countLabels[i] = new JLabel();
            buildingRow.add(countLabels[i], BorderLayout.CENTER);
            add(buildingRow);
        }
        countsChanged();
    }
}

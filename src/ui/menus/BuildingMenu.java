package src.ui.menus;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import src.managers.BuildingManager;
import src.managers.CropManager;
import src.managers.TileManager;

public class BuildingMenu {
    private final JPanel parent;
    private final TileManager tileManager;
    private final BuildingManager buildingManager;
    private final CropManager cropManager;

    public BuildingMenu(JPanel parent, TileManager tileManager, BuildingManager buildingManager,
            CropManager cropManager) {
        this.parent = parent;
        this.tileManager = tileManager;
        this.buildingManager = buildingManager;
        this.cropManager = cropManager;
    }

    public JMenu create(int col, int row) {
        JMenu buildingsMenu = new JMenu("buildings");
        boolean unavailable = tileManager.isWaterTile(row, col)
            || tileManager.isLavaTile(row, col)
                || isOccupied(row, col);
        buildingsMenu.setEnabled(!unavailable);

        for (int i = 0; i < BuildingManager.BUILDING_COUNT; i++) {
            Image building = buildingManager.getBuildingImage(i);
            JMenuItem buildingChoice = new JMenuItem(buildingManager.getBuildingName(i));
            if (building != null) {
                Image thumbnail = building.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                buildingChoice.setIcon(new ImageIcon(thumbnail));
            }
            buildingChoice.setEnabled(!unavailable);
            int buildingIndex = i;
            buildingChoice.addActionListener(action -> placeBuilding(row, col, buildingIndex));
            buildingsMenu.add(buildingChoice);
        }
        return buildingsMenu;
    }

    public void placeBuilding(int row, int col, int buildingIndex) {
        if (tileManager.isWaterTile(row, col) || tileManager.isLavaTile(row, col)
            || isOccupied(row, col)) {
            return;
        }
        buildingManager.placeBuilding(row, col, buildingIndex);
        parent.repaint();
    }

    private boolean isOccupied(int row, int col) {
        return buildingManager.hasBuilding(row, col) || cropManager.hasCrop(row, col);
    }
}

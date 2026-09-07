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

    // create a menu for constructing buildings
    public JMenu create(int col, int row) {
        JMenu buildingsMenu = new JMenu("buildings");
        boolean unavailable = tileManager.isWaterTile(row, col)
            || tileManager.isLavaTile(row, col);
        buildingsMenu.setEnabled(!unavailable);

        // add menu items for each building ytype
        for (int i = 0; i < BuildingManager.BUILDING_COUNT; i++) {
            Image building = buildingManager.getBuildingImage(i);
            JMenuItem buildingChoice = new JMenuItem(buildingManager.getBuildingName(i));
            if (building != null) {
                Image thumbnail = building.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                buildingChoice.setIcon(new ImageIcon(thumbnail));
            }
                buildingChoice.setEnabled(!unavailable
                    && (i == BuildingManager.FENCE_INDEX || !isOccupied(row, col)));
            int buildingIndex = i;
            buildingChoice.addActionListener(action -> placeBuilding(row, col, buildingIndex));
            buildingsMenu.add(buildingChoice);
        }
        return buildingsMenu;
    }

    public void placeBuilding(int row, int col, int buildingIndex) {
        placeBuilding(row, col, buildingIndex, BuildingManager.FENCE_BOTTOM);
    }

    public void placeBuilding(int row, int col, int buildingIndex, int fenceSide) {
        if (!canPlaceBuilding(row, col, buildingIndex)) {
            return;
        }
        buildingManager.placeBuilding(row, col, buildingIndex, fenceSide);
        parent.repaint();
    }

    public boolean canPlaceBuilding(int row, int col) {
        return canPlaceBuilding(row, col, BuildingManager.FENCE_INDEX);
    }

    public boolean canPlaceBuilding(int row, int col, int buildingIndex) {
        return !tileManager.isWaterTile(row, col) && !tileManager.isLavaTile(row, col)
                && (buildingIndex == BuildingManager.FENCE_INDEX || !isOccupied(row, col));
    }

    private boolean isOccupied(int row, int col) {
        return buildingManager.hasBuilding(row, col) || cropManager.hasCrop(row, col);
    }
}

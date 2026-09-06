package src.ui.menus;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import src.managers.BuildingManager;
import src.managers.CropManager;
import src.managers.TileManager;

public class CropMenu {
    private final JPanel parent;
    private final TileManager tileManager;
    private final BuildingManager buildingManager;
    private final CropManager cropManager;

    public CropMenu(JPanel parent, TileManager tileManager, BuildingManager buildingManager,
            CropManager cropManager) {
        this.parent = parent;
        this.tileManager = tileManager;
        this.buildingManager = buildingManager;
        this.cropManager = cropManager;
    }

    public JMenu create(int col, int row) {
        JMenu cropsMenu = new JMenu("crops");
        boolean canPlant = tileManager.isSoilTile(row, col) && !isOccupied(row, col);
        cropsMenu.setEnabled(canPlant);

        for (int i = 0; i < CropManager.CROP_COUNT; i++) {
            JMenuItem cropChoice = new JMenuItem(cropManager.getCropName(i));
            cropChoice.setForeground(cropManager.getCropColor(i));
            cropChoice.setEnabled(canPlant);
            int cropIndex = i;
            cropChoice.addActionListener(action -> plantCrop(row, col, cropIndex));
            cropsMenu.add(cropChoice);
        }
        return cropsMenu;
    }

    public void plantCrop(int row, int col, int cropIndex) {
        if (!tileManager.isSoilTile(row, col) || isOccupied(row, col)) {
            return;
        }
        cropManager.plantCrop(row, col, cropIndex);
        parent.repaint();
    }

    private boolean isOccupied(int row, int col) {
        return buildingManager.hasBuilding(row, col) || cropManager.hasCrop(row, col);
    }
}

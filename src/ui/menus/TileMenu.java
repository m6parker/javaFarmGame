package src.ui.menus;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JWindow;

import src.managers.BuildingManager;
import src.managers.CropManager;
import src.managers.TileManager;

import javax.swing.JTextArea;
import javax.swing.BorderFactory;

public class TileMenu {
    private final JPanel parent;
    private final TileManager tileManager;
    private final BuildingManager buildingManager;
    private final CropManager cropManager;
    private final BuildingMenu buildingMenu;
    private final CropMenu cropMenu;
    private JWindow informationBox;

    // constructor
    public TileMenu(JPanel parent, TileManager tileManager, BuildingManager buildingManager,
            CropManager cropManager) {
        this.parent = parent;
        this.tileManager = tileManager;
        this.buildingManager = buildingManager;
        this.cropManager = cropManager;
        this.buildingMenu = new BuildingMenu(parent, tileManager, buildingManager, cropManager);
        this.cropMenu = new CropMenu(parent, tileManager, buildingManager, cropManager);
    }

    public void placeBuilding(int col, int row, int buildingIndex) {
        buildingMenu.placeBuilding(row, col, buildingIndex);
    }

    public void placeCrop(int col, int row, int cropIndex) {
        cropMenu.plantCrop(row, col, cropIndex);
    }

    public void paintTerrain(int col, int row, java.awt.Color terrainColor) {
        if (isOccupied(row, col)) {
            return;
        }
        tileManager.setColor(row, col, terrainColor);
        parent.repaint();
    }

    public void bulldoze(int col, int row) {
        buildingManager.removeBuilding(row, col);
        cropManager.removeCrop(row, col);
        hideInformationBox();
        parent.repaint();
    }

    public void harvest(int col, int row) {
        if (cropManager.harvestCrop(row, col)) {
            parent.repaint();
        }
    }

    public void showInformation(MouseEvent event, int col, int row) {
        showInformationBox(event, row, col);
    }

    public void hideInformationBox() {
        if (informationBox != null) {
            informationBox.setVisible(false);
        }
    }

    // shows tile details and occupancy
    private void showInformationBox(MouseEvent event, int row, int col) {
        StringBuilder information = new StringBuilder("tile info\n");
        information.append("terrain: ").append(tileManager.getTerrainName(row, col)).append('\n');
        information.append("temperature: ").append(tileManager.getTemperature(row, col))
            .append("%\n");
        information.append("moisture: ").append(tileManager.getMoisture(row, col))
            .append("%\n");
        information.append("nutrients: ").append(tileManager.getNutrients(row, col))
            .append("%\n");
        String buildingName = buildingManager.getBuildingAt(row, col);
        String cropName = cropManager.getCropAt(row, col);

        information.append("building: ").append(buildingName == null ? "none" : buildingName)
            .append('\n');
        information.append("crop: ").append(cropName == null ? "none" : cropName);
        if (cropName != null) {
            information.append(" (stage ").append(cropManager.getCropStage(row, col) + 1)
                .append('/').append(CropManager.MATURE_STAGE + 1).append(')');
        }

        JTextArea textBox = new JTextArea(information.toString());
        textBox.setEditable(false);
        textBox.setFocusable(false);
        textBox.setOpaque(true);
        textBox.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        if (informationBox == null) {
            informationBox = new JWindow();
        }
        informationBox.setContentPane(textBox);
        informationBox.pack();
        Point location = parent.getLocationOnScreen();
        informationBox.setLocation(location.x + event.getX() + 12,
                location.y + event.getY() + 12);
        informationBox.setVisible(true);
    }

    private boolean isOccupied(int row, int col) {
        return buildingManager.hasBuilding(row, col) || cropManager.hasCrop(row, col);
    }
}

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
    private final BuildingManager buildingManager;
    private final CropManager cropManager;
    private final BuildingMenu buildingMenu;
    private final CropMenu cropMenu;
    private JWindow informationBox;

    // constructor
    public TileMenu(JPanel parent, TileManager tileManager, BuildingManager buildingManager,
            CropManager cropManager) {
        this.parent = parent;
        this.buildingManager = buildingManager;
        this.cropManager = cropManager;
        this.buildingMenu = new BuildingMenu(parent, tileManager, buildingManager, cropManager);
        this.cropMenu = new CropMenu(parent, tileManager, buildingManager, cropManager);
    }

    public void showConstructionMenu(MouseEvent event, int col, int row) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(buildingMenu.create(col, row));
        menu.show(parent, event.getX(), event.getY());
    }

    public void placeBuilding(int col, int row, int buildingIndex) {
        buildingMenu.placeBuilding(row, col, buildingIndex);
    }

    public void placeCrop(int col, int row, int cropIndex) {
        cropMenu.plantCrop(row, col, cropIndex);
    }

    public void bulldoze(int col, int row) {
        buildingManager.removeBuilding(row, col);
        cropManager.removeCrop(row, col);
        hideInformationBox();
        parent.repaint();
    }

    public void showInformation(MouseEvent event, int col, int row) {
        showInformationBox(event, row, col);
    }

    public void showInformationOnHover(MouseEvent event, int col, int row) {
        showInformationBox(event, row, col);
    }

    public void hideInformationBox() {
        if (informationBox != null) {
            informationBox.setVisible(false);
        }
    }

    // shows tile info when occupied
    private void showInformationBox(MouseEvent event, int row, int col) {
        if (!isOccupied(row, col)) {
            hideInformationBox();
            return;
        }

        StringBuilder information = new StringBuilder("tile info\n");
        String buildingName = buildingManager.getBuildingAt(row, col);
        String cropName = cropManager.getCropAt(row, col);

        if (buildingName != null) {
            information.append("building: ").append(buildingName);
        }
        if (cropName != null) {
            information.append("crop: ").append(cropName);
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

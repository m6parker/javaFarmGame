import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;

public class TileMenu {
    private static final String[] TERRAIN_NAMES = {
            "grass",
            "water",
            "lava",
            "sand",
            "soil",
            "stone"
    };
    private static final Color[] TERRAIN_COLORS = {
            new Color(76, 175, 80), // green
            new Color(66, 135, 245), // blue
            new Color(220, 70, 70), // red
            new Color(245, 205, 60), // yellow
            new Color(145, 95, 55), // brown
            new Color(150, 150, 150) // grey
    };
    private static final Color WATER_COLOR = new Color(66, 135, 245);
    private static final Color LAVA_COLOR = new Color(220, 70, 70);
    private static final Color SOIL_COLOR = new Color(145, 95, 55);

    private final JPanel parent;
    private final Color[][] tileColors;
    private final BuildingManager buildingManager;
    private final CropManager cropManager;
    private JWindow informationBox;

    // constructor
    public TileMenu(JPanel parent, Color[][] tileColors, BuildingManager buildingManager,
            CropManager cropManager) {
        this.parent = parent;
        this.tileColors = tileColors;
        this.buildingManager = buildingManager;
        this.cropManager = cropManager;
    }

    // shows menu when clicking on a tile
    public void show(MouseEvent event, int col, int row) {
        showInformationBox(event, row, col);

        if (isOccupied(row, col)) {
            return;
        }

        JPopupMenu menu = new JPopupMenu();
        JMenu terrainMenu = createTerrainMenu(col, row);
        menu.add(terrainMenu);
        menu.addSeparator();
        menu.add(createBuildingsMenu(col, row));
        menu.add(createCropsMenu(col, row));
        menu.show(parent, event.getX(), event.getY());
    }

    public void showInformationOnHover(MouseEvent event, int col, int row) {
        showInformationBox(event, row, col);
    }

    public void hideInformationBox() {
        if (informationBox != null) {
            informationBox.setVisible(false);
        }
    }

    // creates menu for changing terrain type
    private JMenu createTerrainMenu(int col, int row) {
        JMenu terrainMenu = new JMenu("terrain");
        boolean occupied = isOccupied(row, col);
        terrainMenu.setEnabled(!occupied);
        // add menu items for each terrain type
        for (int i = 0; i < TERRAIN_NAMES.length; i++) {
            Color terrainColor = TERRAIN_COLORS[i];
            JMenuItem terrainChoice = new JMenuItem(TERRAIN_NAMES[i]);
            terrainChoice.setEnabled(!occupied);
            terrainChoice.setOpaque(true);
            terrainChoice.setBackground(terrainColor);
            terrainChoice.setForeground(terrainColor.getRed()
                    + terrainColor.getGreen() + terrainColor.getBlue() > 400
                    ? Color.BLACK
                    : Color.WHITE);
            terrainChoice.addActionListener(action -> {
                if (isOccupied(row, col)) {
                    return;
                }
                tileColors[row][col] = terrainColor;
                parent.repaint();
            });
            terrainMenu.add(terrainChoice);
        }
        return terrainMenu;
    }

    // creates menu for placing buildings
    private JMenu createBuildingsMenu(int col, int row) {
        JMenu buildingsMenu = new JMenu("buildings");
        boolean unbuildableTile = isBlueTile(row, col)
            || isLavaTile(row, col)
            || isOccupied(row, col);
        buildingsMenu.setEnabled(!unbuildableTile);

        // add menu items for each building type
        for (int i = 0; i < BuildingManager.BUILDING_COUNT; i++) {
            Image building = buildingManager.getBuildingImage(i);
            JMenuItem buildingChoice = new JMenuItem(buildingManager.getBuildingName(i));
            if (building != null) {
                Image thumbnail = building.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                buildingChoice.setIcon(new ImageIcon(thumbnail));
            }
            buildingChoice.setEnabled(!unbuildableTile);
            int buildingIndex = i;
            // only allow buildings on tiles that are not water / lava / occupied
            buildingChoice.addActionListener(action -> {
                if (isBlueTile(row, col) || isLavaTile(row, col) || isOccupied(row, col)) {
                    return;
                }
                buildingManager.placeBuilding(row, col, buildingIndex);
                parent.repaint();
            });
            buildingsMenu.add(buildingChoice);
        }
        return buildingsMenu;
    }

    // creates menu for planting crops
    private JMenu createCropsMenu(int col, int row) {
        JMenu cropsMenu = new JMenu("crops");
        boolean canPlant = isSoilTile(row, col) && !isOccupied(row, col);
        cropsMenu.setEnabled(canPlant);

        // add menu items for each crop type
        for (int i = 0; i < CropManager.CROP_COUNT; i++) {
            JMenuItem cropChoice = new JMenuItem(cropManager.getCropName(i));
            cropChoice.setForeground(cropManager.getCropColor(i));
            cropChoice.setEnabled(canPlant);
            int cropIndex = i;
            // only allow crops on soil tiles that are not occupied
            cropChoice.addActionListener(action -> {
                if (isSoilTile(row, col) && !isOccupied(row, col)) {
                    cropManager.plantCrop(row, col, cropIndex);
                    parent.repaint();
                }
            });
            cropsMenu.add(cropChoice);
        }
        return cropsMenu;
    }

    private boolean isBlueTile(int row, int col) {
        return tileColors[row][col].equals(WATER_COLOR);
    }

    private boolean isLavaTile(int row, int col) {
        return tileColors[row][col].equals(LAVA_COLOR);
    }

    private boolean isSoilTile(int row, int col) {
        return tileColors[row][col].equals(SOIL_COLOR);
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

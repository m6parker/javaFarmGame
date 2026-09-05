import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BuildingManager {
    public static final int BUILDING_COUNT = 6;
    private static final String[] BUILDING_NAMES = {
            "house",
            "workshop",
            "tower",
            "store",
            "fountain",
            "chapel"
    };

    private final int[][] tileBuildings;
    private final BufferedImage[][] buildingTiles = new BufferedImage[2][3];
    private final int[] buildingCounts = new int[BUILDING_COUNT];
    private final JLabel[] buildingCountLabels = new JLabel[BUILDING_COUNT];

    public BuildingManager(int rows, int cols) {
        tileBuildings = new int[rows][cols];
        initializeBuildingTiles();
    }

    public void draw(Graphics2D graphics, int row, int col, int x, int y, int tileSize) {
        int buildingIndex = tileBuildings[row][col];
        if (buildingIndex >= 0) {
            BufferedImage building = getBuildingImage(buildingIndex);
            graphics.drawImage(building, x, y, tileSize, tileSize, null);
        }
    }

    public BufferedImage getBuildingImage(int buildingIndex) {
        return buildingTiles[buildingIndex / 3][buildingIndex % 3];
    }

    public String getBuildingName(int buildingIndex) {
        return BUILDING_NAMES[buildingIndex];
    }

    public boolean hasBuilding(int row, int col) {
        return tileBuildings[row][col] >= 0;
    }

    public String getBuildingAt(int row, int col) {
        int buildingIndex = tileBuildings[row][col];
        return buildingIndex >= 0 ? BUILDING_NAMES[buildingIndex] : null;
    }

    public boolean placeBuilding(int row, int col, int buildingIndex) {
        int previousBuilding = tileBuildings[row][col];
        if (previousBuilding == buildingIndex) {
            return false;
        }
        if (previousBuilding >= 0) {
            buildingCounts[previousBuilding]--;
        }
        tileBuildings[row][col] = buildingIndex;
        buildingCounts[buildingIndex]++;
        updateBuildingCountLabels();
        return true;
    }

    // creates panel to display number of buildings
    public JPanel createPanel(int screenHeight) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("buildings"));
        panel.setLayout(new GridLayout(0, 1, 4, 4));
        panel.setPreferredSize(new Dimension(180, screenHeight));

        for (int i = 0; i < BUILDING_COUNT; i++) {
            JPanel buildingRow = new JPanel(new BorderLayout(6, 0));
            BufferedImage building = getBuildingImage(i);
            if (building != null) {
                Image thumbnail = building.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                buildingRow.add(new JLabel(new ImageIcon(thumbnail)), BorderLayout.WEST);
            }

            buildingCountLabels[i] = new JLabel(BUILDING_NAMES[i] + ": 0");
            buildingRow.add(buildingCountLabels[i], BorderLayout.CENTER);
            panel.add(buildingRow);
        }
        return panel;
    }

    private void initializeBuildingTiles() {
        // create 2D array of building tiles
        for (int row = 0; row < tileBuildings.length; row++) {
            for (int col = 0; col < tileBuildings[row].length; col++) {
                tileBuildings[row][col] = -1;
            }
        }

        // load images from spritesheet and split images
        try {
            BufferedImage atlas = ImageIO.read(new File("img/buildings.png"));
            int tileWidth = atlas.getWidth() / 3;
            int tileHeight = atlas.getHeight() / 2;

            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 3; col++) {
                    buildingTiles[row][col] = atlas.getSubimage(
                            col * tileWidth, row * tileHeight, tileWidth, tileHeight);
                }
            }
        } catch (IOException exception) {
            System.err.println("Could not load img/buildings.png: " + exception.getMessage());
        }
    }

    private void updateBuildingCountLabels() {
        for (int i = 0; i < BUILDING_COUNT; i++) {
            if (buildingCountLabels[i] != null) {
                buildingCountLabels[i].setText(BUILDING_NAMES[i] + ": " + buildingCounts[i]);
            }
        }
    }
}

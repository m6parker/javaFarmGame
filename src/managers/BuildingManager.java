package src.managers;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

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
    private final List<CountListener> countListeners = new ArrayList<>();

    public interface CountListener {
        void countsChanged();
    }

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
        notifyCountListeners();
        return true;
    }

    public int getBuildingCount(int buildingIndex) {
        return buildingCounts[buildingIndex];
    }

    public void addCountListener(CountListener listener) {
        countListeners.add(listener);
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

    private void notifyCountListeners() {
        for (CountListener listener : countListeners) {
            listener.countsChanged();
        }
    }
}

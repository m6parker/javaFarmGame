package src.managers;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

import src.Enums.Colors;
import src.entities.Building;

public class BuildingManager {
    public static final int BUILDING_COUNT = 4;
    public static final int FENCE_INDEX = 3;
    public static final int BUILDING_WOOD_COST = 3;
    public static final int FENCE_WOOD_COST = 1;
    public static final int UPGRADE_WOOD_COST = 2;
    public static final int FENCE_TOP = FenceManager.FENCE_TOP;
    public static final int FENCE_RIGHT = FenceManager.FENCE_RIGHT;
    public static final int FENCE_BOTTOM = FenceManager.FENCE_BOTTOM;
    public static final int FENCE_LEFT = FenceManager.FENCE_LEFT;
    private static final String[] BUILDING_NAMES = {
            "house",
            "tower",
            "barn",
            "fence"
    };

    private final Building[][] tileBuildings;
    private final FenceManager fenceManager;
    private final BufferedImage[][] buildingTiles = new BufferedImage[2][3];
    private final int[] buildingCounts = new int[BUILDING_COUNT];
    private final List<CountListener> countListeners = new ArrayList<>();

    // interface for listening to building count changes
    public interface CountListener {
        void countsChanged();
    }

    // constructor
    public BuildingManager(int rows, int cols) {
        tileBuildings = new Building[rows][cols];
        fenceManager = new FenceManager(rows, cols);
        initializeBuildingTiles();
    }

    // draw building on tile if it exists
    public void draw(Graphics2D graphics, int row, int col, int x, int y, int tileSize) {
        Building placedBuilding = tileBuildings[row][col];
        if (placedBuilding != null) {
            BufferedImage buildingImage = getBuildingImage(placedBuilding.getTypeIndex(), placedBuilding.getLevel());
            if (buildingImage != null) {
                graphics.drawImage(buildingImage, x, y, tileSize, tileSize, null);
            }
        }
        fenceManager.draw(graphics, row, col, x, y, tileSize, Colors.FENCE.color());
    }

    public BufferedImage getBuildingImage(int buildingIndex) {
        if (buildingIndex < 0 || buildingIndex >= FENCE_INDEX) {
            return null;
        }
        return buildingTiles[1][buildingIndex];
    }

    public BufferedImage getBuildingImage(int buildingIndex, int level) {
        if (buildingIndex < 0 || buildingIndex >= FENCE_INDEX) {
            return null;
        }
        int row = level >= 2 ? 0 : 1;
        return buildingTiles[row][buildingIndex];
    }

    public String getBuildingName(int buildingIndex) {
        return BUILDING_NAMES[buildingIndex];
    }

    public boolean hasBuilding(int row, int col) {
        return tileBuildings[row][col] != null;
    }

    public String getBuildingAt(int row, int col) {
        Building building = tileBuildings[row][col];
        return building == null ? null : building.getName();
    }

    // place building on the tile and update count
    public boolean placeBuilding(int row, int col, int buildingIndex) {
        return placeBuilding(row, col, buildingIndex, FENCE_BOTTOM);
    }

    public boolean placeBuilding(int row, int col, int buildingIndex, int fenceSide) {
        if (buildingIndex < 0 || buildingIndex >= BUILDING_COUNT) {
            return false;
        }
        if (buildingIndex == FENCE_INDEX) {
            return placeFence(row, col, fenceSide);
        }
        Building previousBuilding = tileBuildings[row][col];
        if (previousBuilding != null && previousBuilding.getTypeIndex() == buildingIndex) {
            return false;
        }
        if (previousBuilding != null) {
            buildingCounts[previousBuilding.getTypeIndex()]--;
        }
        tileBuildings[row][col] = new Building(buildingIndex, BUILDING_NAMES[buildingIndex]);
        buildingCounts[buildingIndex]++;
        notifyCountListeners();
        return true;
    }

    public boolean removeBuilding(int row, int col) {
        Building building = tileBuildings[row][col];
        if (building == null) {
            return false;
        }
        // remove building from tile
        tileBuildings[row][col] = null;
        //update count
        buildingCounts[building.getTypeIndex()]--;
        notifyCountListeners();
        return true;
    }

    public boolean removeFences(int row, int col) {
        int removedCount = fenceManager.removeFences(row, col);
        if (removedCount == 0) {
            return false;
        }
        buildingCounts[FENCE_INDEX] -= removedCount;
        notifyCountListeners();
        return true;
    }

    public int getBuildingCount(int buildingIndex) {
        return buildingCounts[buildingIndex];
    }

    public int getDwellerCount() {
        int count = 0;
        for (int row = 0; row < tileBuildings.length; row++) {
            for (int col = 0; col < tileBuildings[row].length; col++) {
                count += getDwellerCount(row, col);
            }
        }
        return count;
    }

    public int getDwellerCount(int row, int col) {
        Building building = tileBuildings[row][col];
        if (building == null || building.getTypeIndex() != 0) {
            return 0;
        }
        return building.getLevel() + (hasAdjacentHouse(row, col) ? 1 : 0);
    }

    public int getBuildingLevel(int row, int col) {
        return tileBuildings[row][col] == null ? 0 : tileBuildings[row][col].getLevel();
    }

    public boolean upgradeBuilding(int row, int col) {
        Building building = tileBuildings[row][col];
        if (building == null || building.getLevel() >= 2) {
            return false;
        }
        building.upgrade();
        return true;
    }

    private boolean hasAdjacentHouse(int row, int col) {
        return isHouse(row - 1, col) || isHouse(row + 1, col)
                || isHouse(row, col - 1) || isHouse(row, col + 1);
    }

    private boolean isHouse(int row, int col) {
        return isInBounds(row, col) && tileBuildings[row][col] != null
                && tileBuildings[row][col].getTypeIndex() == 0;
    }

    public void addCountListener(CountListener listener) {
        countListeners.add(listener);
    }

    public boolean placeFence(int row, int col, int side) {
        if (!fenceManager.placeFence(row, col, side)) {
            return false;
        }
        buildingCounts[FENCE_INDEX]++;
        notifyCountListeners();
        return true;
    }

    public void drawFencePreview(Graphics2D graphics, int row, int col, int x, int y,
        int tileSize, int side) {
        fenceManager.drawPreview(graphics, x, y, tileSize, side);
    }

    public boolean hasFence(int row, int col) {
        return fenceManager.hasFence(row, col);
    }

    public boolean hasFenceSide(int row, int col, int side) {
        return fenceManager.hasFenceSide(row, col, side);
    }

    private boolean isInBounds(int row, int col) {
            return row >= 0 && row < tileBuildings.length
                    && col >= 0 && col < tileBuildings[row].length;
        }

    private void initializeBuildingTiles() {
        // create 2D array of building tiles
        for (int row = 0; row < tileBuildings.length; row++) {
            for (int col = 0; col < tileBuildings[row].length; col++) {
                tileBuildings[row][col] = null;
            }
        }

        // load images from spritesheet and split images
        try {
            BufferedImage atlas = ImageIO.read(new File("img/tilesets/buildings.png"));
            if (atlas == null) {
                throw new IOException("image could not be decoded");
            }
            int tileWidth = atlas.getWidth() / 3;
            int tileHeight = atlas.getHeight() / 2;

            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 3; col++) {
                    buildingTiles[row][col] = atlas.getSubimage(
                            col * tileWidth, row * tileHeight, tileWidth, tileHeight);
                }
            }
        } catch (IOException exception) {
            System.err.println("Could not load img/tilesets/buildings.png: "
                    + exception.getMessage());
        }
    }

    // notify listeners when building counts change
    private void notifyCountListeners() {
        for (CountListener listener : countListeners) {
            listener.countsChanged();
        }
    }
}

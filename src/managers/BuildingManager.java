package src.managers;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import src.entities.buildings.Building;

public class BuildingManager {
    public static final int BUILDING_COUNT = 4;
    public static final int FENCE_INDEX = 3;
    public static final int BUILDING_WOOD_COST = 3;
    public static final int FENCE_WOOD_COST = 1;
    public static final int UPGRADE_WOOD_COST = 2;
    public static final int FENCE_TOP = 0;
    public static final int FENCE_RIGHT = 1;
    public static final int FENCE_BOTTOM = 2;
    public static final int FENCE_LEFT = 3;
    private static final String[] BUILDING_NAMES = {
            "house",
            "tower",
            "barn",
            "fence"
    };

    private final Building[][] tileBuildings;
    private final int[][] tileFenceSides;
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
        tileFenceSides = new int[rows][cols];
        initializeBuildingTiles();
    }

    // draw building on tile if it exists
    public void draw(Graphics2D graphics, int row, int col, int x, int y, int tileSize) {
        Building placedBuilding = tileBuildings[row][col];
        if (placedBuilding != null) {
            BufferedImage building = getBuildingImage(placedBuilding.getTypeIndex(),
                    placedBuilding.getLevel());
            if (building != null) {
                int buildingSize = tileSize * placedBuilding.getSize();
                int buildingX = x - (buildingSize - tileSize) / 2;
                int buildingY = y - (buildingSize - tileSize) / 2;
                graphics.drawImage(building, buildingX, buildingY,
                        buildingSize, buildingSize, null);
            }
        }
        drawFence(graphics, row, col, x, y, tileSize, new Color(64, 16, 45));
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
        int sides = tileFenceSides[row][col];
        if (sides == 0) {
            return false;
        }
        for (int side = FENCE_TOP; side <= FENCE_LEFT; side++) {
            if ((sides & (1 << side)) != 0) {
                int neighborRow = row;
                int neighborCol = col;
                int neighborSide = side;
                if (side == FENCE_TOP) {
                    neighborRow--;
                    neighborSide = FENCE_BOTTOM;
                } else if (side == FENCE_RIGHT) {
                    neighborCol++;
                    neighborSide = FENCE_LEFT;
                } else if (side == FENCE_BOTTOM) {
                    neighborRow++;
                    neighborSide = FENCE_TOP;
                } else {
                    neighborCol--;
                    neighborSide = FENCE_RIGHT;
                }
                if (isInBounds(neighborRow, neighborCol)) {
                    tileFenceSides[neighborRow][neighborCol] &= ~(1 << neighborSide);
                }
            }
        }
        tileFenceSides[row][col] = 0;
        buildingCounts[FENCE_INDEX] -= Integer.bitCount(sides);
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

    public int getBuildingSize(int row, int col) {
        return tileBuildings[row][col] == null ? 0 : tileBuildings[row][col].getSize();
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
        if (hasFenceSide(row, col, side)) {
            return false;
        }
        tileFenceSides[row][col] |= 1 << side;
        int neighborRow = row;
        int neighborCol = col;
        int neighborSide = side;
        if (side == FENCE_TOP) {
            neighborRow--;
            neighborSide = FENCE_BOTTOM;
        } else if (side == FENCE_RIGHT) {
            neighborCol++;
            neighborSide = FENCE_LEFT;
        } else if (side == FENCE_BOTTOM) {
            neighborRow++;
            neighborSide = FENCE_TOP;
        } else if (side == FENCE_LEFT) {
            neighborCol--;
            neighborSide = FENCE_RIGHT;
        }
        if (isInBounds(neighborRow, neighborCol)) {
            tileFenceSides[neighborRow][neighborCol] |= 1 << neighborSide;
        }
        buildingCounts[FENCE_INDEX]++;
        notifyCountListeners();
        return true;
    }

    public boolean hasFence(int row, int col) {
        return tileFenceSides[row][col] != 0;
    }

        public void drawFencePreview(Graphics2D graphics, int row, int col, int x, int y,
            int tileSize, int side) {
            drawFenceSide(graphics, x, y, tileSize, side, new Color(64, 16, 45, 150));
        }

        private void drawFence(Graphics2D graphics, int row, int col, int x, int y,
                int tileSize, Color color) {
            int sides = tileFenceSides[row][col];
            for (int side = FENCE_TOP; side <= FENCE_LEFT; side++) {
                if ((sides & (1 << side)) != 0 && shouldDrawSide(row, col, side)) {
                    drawFenceSide(graphics, x, y, tileSize, side, color);
                }
            }
        }

        private void drawFenceSide(Graphics2D graphics, int x, int y, int tileSize,
                int side, Color color) {
        Stroke previousStroke = graphics.getStroke();
        Color previousColor = graphics.getColor();
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(Math.max(2f, tileSize / 8f)));
        int inset = Math.max(2, tileSize / 16);
            if (side == FENCE_TOP) {
                graphics.drawLine(x, y, x + tileSize, y);
            } else if (side == FENCE_RIGHT) {
                graphics.drawLine(x + tileSize, y, x + tileSize, y + tileSize);
            } else if (side == FENCE_BOTTOM) {
                graphics.drawLine(x, y + tileSize, x + tileSize, y + tileSize);
            } else {
                graphics.drawLine(x, y, x, y + tileSize);
            }
        graphics.setStroke(previousStroke);
        graphics.setColor(previousColor);
        }

        private boolean shouldDrawSide(int row, int col, int side) {
            return side == FENCE_TOP || side == FENCE_LEFT
                    || side == FENCE_BOTTOM && row == tileBuildings.length - 1
                    || side == FENCE_RIGHT && col == tileBuildings[row].length - 1;
    }

        public boolean hasFenceSide(int row, int col, int side) {
        return row >= 0 && row < tileBuildings.length
            && col >= 0 && col < tileBuildings[row].length
                && (tileFenceSides[row][col] & (1 << side)) != 0;
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

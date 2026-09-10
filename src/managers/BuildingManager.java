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

public class BuildingManager {
    public static final int BUILDING_COUNT = 7;
    public static final int FENCE_INDEX = 6;
    public static final int FENCE_TOP = 0;
    public static final int FENCE_RIGHT = 1;
    public static final int FENCE_BOTTOM = 2;
    public static final int FENCE_LEFT = 3;
    private static final String[] BUILDING_NAMES = {
            "house",
            "workshop",
            "tower",
            "store",
            "fountain",
            "chapel",
            "fence"
    };

    private final int[][] tileBuildings;
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
        tileBuildings = new int[rows][cols];
        tileFenceSides = new int[rows][cols];
        initializeBuildingTiles();
    }

    // draw building on tile if it exists
    public void draw(Graphics2D graphics, int row, int col, int x, int y, int tileSize) {
        int buildingIndex = tileBuildings[row][col];
        if (buildingIndex >= 0) {
            BufferedImage building = getBuildingImage(buildingIndex);
            if (building != null) {
                graphics.drawImage(building, x, y, tileSize, tileSize, null);
            }
        }
        drawFence(graphics, row, col, x, y, tileSize, new Color(105, 68, 35));
    }

    public BufferedImage getBuildingImage(int buildingIndex) {
        if (buildingIndex == FENCE_INDEX) {
            return null;
        }
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

    // place building on the tile and update count
    public boolean placeBuilding(int row, int col, int buildingIndex) {
        return placeBuilding(row, col, buildingIndex, FENCE_BOTTOM);
    }

    public boolean placeBuilding(int row, int col, int buildingIndex, int fenceSide) {
        if (buildingIndex == FENCE_INDEX) {
            return placeFence(row, col, fenceSide);
        }
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

    public boolean removeBuilding(int row, int col) {
        int buildingIndex = tileBuildings[row][col];
        if (buildingIndex < 0) {
            return false;
        }
        // remove building from tile
        tileBuildings[row][col] = -1;
        //update count
        buildingCounts[buildingIndex]--;
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
        // count all buildings except fences
        for (int buildingIndex = 0; buildingIndex < FENCE_INDEX; buildingIndex++) {
            count += buildingCounts[buildingIndex];
        }
        return count;
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
            drawFenceSide(graphics, x, y, tileSize, side, new Color(105, 68, 35, 150));
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

    // notify listeners when building counts change
    private void notifyCountListeners() {
        for (CountListener listener : countListeners) {
            listener.countsChanged();
        }
    }
}

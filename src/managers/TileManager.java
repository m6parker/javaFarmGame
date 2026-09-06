package src.managers;

import java.awt.Color;
import java.util.Random;
import src.entities.Tile;

public class TileManager {
    public static final Color GRASS_COLOR = new Color(76, 175, 80);
    public static final Color WATER_COLOR = new Color(66, 135, 245);
    public static final Color LAVA_COLOR = new Color(220, 70, 70);
    public static final Color SOIL_COLOR = new Color(145, 95, 55);
    public static final Color SAND_COLOR = new Color(245, 205, 60);
    public static final Color STONE_COLOR = new Color(150, 150, 150);
    private static final Color[] TERRAIN_COLORS = {
        GRASS_COLOR, WATER_COLOR, LAVA_COLOR, SAND_COLOR, SOIL_COLOR, STONE_COLOR
    };

    private final Tile[][] tiles;

    public TileManager(int rows, int cols) {
        tiles = new Tile[rows][cols];
        initializeTiles();
    }

    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    public Color getColor(int row, int col) {
        return getTile(row, col).getTerrainColor();
    }

    public void setColor(int row, int col, Color color) {
        getTile(row, col).setTerrainColor(color);
    }

    public static Color getTerrainColor(int terrainIndex) {
        return TERRAIN_COLORS[terrainIndex];
    }

    public boolean isWaterTile(int row, int col) {
        return getColor(row, col).equals(WATER_COLOR);
    }

    public boolean isLavaTile(int row, int col) {
        return getColor(row, col).equals(LAVA_COLOR);
    }

    public boolean isSoilTile(int row, int col) {
        return getColor(row, col).equals(SOIL_COLOR);
    }

    public int[] findRandomTile(Color color, Random random) {
        for (int attempt = 0; attempt < getRowCount() * getColumnCount(); attempt++) {
            int col = random.nextInt(getColumnCount());
            int row = random.nextInt(getRowCount());
            if (getColor(row, col).equals(color)) {
                return new int[]{col, row};
            }
        }

        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                if (getColor(row, col).equals(color)) {
                    return new int[]{col, row};
                }
            }
        }
        return null;
    }

    public int getRowCount() {
        return tiles.length;
    }

    public int getColumnCount() {
        return tiles[0].length;
    }

    private void initializeTiles() {
        Random random = new Random();
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                int colorRoll = random.nextInt(20);
                Color color = colorRoll < 12 ? GRASS_COLOR
                        : colorRoll < 17 ? SOIL_COLOR
                        : colorRoll < 19 ? WATER_COLOR
                        : STONE_COLOR;
                tiles[row][col] = new Tile(color);
            }
        }

        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                if (!isWaterTile(row, col) && isNextToColor(row, col, WATER_COLOR)
                        && random.nextInt(4) == 0) {
                    setColor(row, col, SAND_COLOR);
                }
            }
        }
    }

    private boolean isNextToColor(int row, int col, Color color) {
        return (row > 0 && getColor(row - 1, col).equals(color))
                || (row < getRowCount() - 1 && getColor(row + 1, col).equals(color))
                || (col > 0 && getColor(row, col - 1).equals(color))
                || (col < getColumnCount() - 1 && getColor(row, col + 1).equals(color));
    }
}

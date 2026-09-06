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
    private static final String[] TERRAIN_NAMES = {
        "grass", "water", "lava", "sand", "soil", "stone"
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

    public String getTerrainName(int row, int col) {
        for (int i = 0; i < TERRAIN_COLORS.length; i++) {
            if (getColor(row, col).equals(TERRAIN_COLORS[i])) {
                return TERRAIN_NAMES[i];
            }
        }
        return "unknown";
    }

    public int getTemperature(int row, int col) {
        return getTile(row, col).getTemperature();
    }

    public int getMoisture(int row, int col) {
        return getTile(row, col).getMoisture();
    }

    public int getNutrients(int row, int col) {
        return getTile(row, col).getNutrients();
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

    public boolean isGrassTile(int row, int col) {
        return getColor(row, col).equals(GRASS_COLOR);
    }

    public int[] findRandomTile(Color color, Random random) {
        // randomly search for tile with color
        for (int attempt = 0; attempt < getRowCount() * getColumnCount(); attempt++) {
            int col = random.nextInt(getColumnCount());
            int row = random.nextInt(getRowCount());
            if (getColor(row, col).equals(color)) {
                return new int[]{col, row};
            }
        }

        // if no tile found search the entire grid for a tile with the specified color
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

    // create tiles with random terrain colors and properties
    private void initializeTiles() {
        Random random = new Random();
        // randomly assign terrain colors
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                int colorRoll = random.nextInt(20);
                Color color = colorRoll < 12 ? GRASS_COLOR
                        : colorRoll < 17 ? SOIL_COLOR
                        : colorRoll < 19 ? WATER_COLOR
                        : STONE_COLOR;
                tiles[row][col] = new Tile(
                    color,
                    random.nextInt(101),
                    random.nextInt(101),
                    random.nextInt(101));
            }
        }

        // add sand tiles next to water tiles
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                if (!isWaterTile(row, col) && isNextToColor(row, col, WATER_COLOR)
                        && random.nextInt(4) == 0) {
                    setColor(row, col, SAND_COLOR);
                }
            }
        }
    }

    // check the color of the tiles adjacent to the specified tile
    private boolean isNextToColor(int row, int col, Color color) {
        return (row > 0 && getColor(row - 1, col).equals(color))
                || (row < getRowCount() - 1 && getColor(row + 1, col).equals(color))
                || (col > 0 && getColor(row, col - 1).equals(color))
                || (col < getColumnCount() - 1 && getColor(row, col + 1).equals(color));
    }
}

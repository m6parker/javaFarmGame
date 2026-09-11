package src.managers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
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
    private static final int MOISTURE_DISTANCE_LIMIT = 3;

    private final Tile[][] tiles;
    private final BufferedImage[] terrainImages = new BufferedImage[TERRAIN_NAMES.length];

    public TileManager(int rows, int cols) {
        tiles = new Tile[rows][cols];
        loadTerrainImages();
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
        updateMoisture();
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

    public void drawTile(Graphics2D graphics, int row, int col, int x, int y, int tileSize) {
        BufferedImage terrainImage = getTerrainImage(row, col);
        if (terrainImage != null) {
            graphics.drawImage(terrainImage, x, y, tileSize, tileSize, null);
            return;
        }

        graphics.setColor(getColor(row, col));
        graphics.fillRect(x, y, tileSize, tileSize);
    }

    private BufferedImage getTerrainImage(int row, int col) {
        String terrainName = getTerrainName(row, col);
        int terrainIndex = -1;
        for (int index = 0; index < TERRAIN_NAMES.length; index++) {
            if (TERRAIN_NAMES[index].equals(terrainName)) {
                terrainIndex = index;
                break;
            }
        }
        return terrainIndex < 0 ? null : terrainImages[terrainIndex];
    }

    private void loadTerrainImages() {
        for (int index = 0; index < TERRAIN_NAMES.length; index++) {
            String imagePath = "img/tiles/" + TERRAIN_NAMES[index] + ".png";
            try {
                terrainImages[index] = ImageIO.read(new File(imagePath));
            } catch (IOException exception) {
                if (index != 4) {
                    System.err.println("Could not load " + imagePath + ": "
                            + exception.getMessage());
                }
            }
        }
    }

    // create tiles with random terrain colors and properties
    private void initializeTiles() {
        Random random = new Random();
        Color[][] terrain = new Color[getRowCount()][getColumnCount()];
        int[][] temperatures = new int[getRowCount()][getColumnCount()];
        int[][] nutrients = new int[getRowCount()][getColumnCount()];

        // randomly assign terrain colors
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                int colorRoll = random.nextInt(20);
                Color color = colorRoll < 12 ? GRASS_COLOR
                        : colorRoll < 17 ? SOIL_COLOR
                        : colorRoll < 19 ? WATER_COLOR
                        : STONE_COLOR;
                terrain[row][col] = color;
                temperatures[row][col] = random.nextInt(101);
                nutrients[row][col] = random.nextInt(101);
            }
        }

        // assign moisture from the distance to the nearest water tile
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                tiles[row][col] = new Tile(
                    terrain[row][col],
                    temperatures[row][col],
                    getMoistureForDistance(row, col, terrain, random),
                    nutrients[row][col]);
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

    private int getMoistureForDistance(int row, int col, Color[][] terrain, Random random) {
        int distanceToWater = getDistanceToWater(row, col, terrain);
        if (distanceToWater > MOISTURE_DISTANCE_LIMIT) {
            return random.nextInt(51);
        }

        int minimum = Math.max(51, 100 - distanceToWater * 16);
        int maximum = Math.max(minimum, 100 - distanceToWater * 12);
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    private void updateMoisture() {
        Color[][] terrain = new Color[getRowCount()][getColumnCount()];
        Random random = new Random();
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                terrain[row][col] = getColor(row, col);
            }
        }

        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                getTile(row, col).setMoisture(
                    getMoistureForDistance(row, col, terrain, random));
            }
        }
    }

    private int getDistanceToWater(int row, int col, Color[][] terrain) {
        int closestDistance = Integer.MAX_VALUE;
        for (int waterRow = 0; waterRow < terrain.length; waterRow++) {
            for (int waterCol = 0; waterCol < terrain[waterRow].length; waterCol++) {
                if (terrain[waterRow][waterCol].equals(WATER_COLOR)) {
                    int distance = Math.abs(row - waterRow) + Math.abs(col - waterCol);
                    closestDistance = Math.min(closestDistance, distance);
                }
            }
        }
        return closestDistance;
    }

    // check the color of the tiles adjacent to the specified tile
    private boolean isNextToColor(int row, int col, Color color) {
        return (row > 0 && getColor(row - 1, col).equals(color))
                || (row < getRowCount() - 1 && getColor(row + 1, col).equals(color))
                || (col > 0 && getColor(row, col - 1).equals(color))
                || (col < getColumnCount() - 1 && getColor(row, col + 1).equals(color));
    }
}

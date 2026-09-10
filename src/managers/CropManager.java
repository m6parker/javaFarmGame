package src.managers;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class CropManager {
    public static final int CROP_COUNT = 2;
    public static final int SEED_STAGE = 0;
    public static final int SPROUT_STAGE = 1;
    public static final int GROWING_STAGE = 2;
    public static final int MATURE_STAGE = 3;
    private static final int FASTEST_GROWTH_TICKS_PER_STAGE = 120;
    private static final int SLOWEST_GROWTH_TICKS_PER_STAGE = 240;
    private static final String[] CROP_NAMES = {"carrot", "tree"};
    // text color in crop selection menu
    private static final Color CARROT_COLOR = new Color(235, 125, 45);
    private static final Color TREE_COLOR = new Color(34, 139, 34);

    private final int[][] tileCrops;
    private final int[][] cropStages;
    private final int[][] growthTicks;
    private final int[] harvestedCropCounts = new int[CROP_COUNT];
    private final List<CountListener> countListeners = new ArrayList<>();
    private BufferedImage carrotImage;
    private BufferedImage treeImage;

    public interface CountListener {
        void countsChanged();
    }

    // constructor
    public CropManager(int rows, int cols) {
        // create 2D array of crops
        tileCrops = new int[rows][cols];
        cropStages = new int[rows][cols];
        growthTicks = new int[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                tileCrops[row][col] = -1;
            }
        }
        try {
            // load image for crop from file
            carrotImage = ImageIO.read(new File("img/carrot.png"));
            treeImage = ImageIO.read(new File("img/tree.png"));
        } catch (IOException exception) {
            System.err.println("Could not load image: " + exception.getMessage());
        }
    }

    public String getCropName(int cropIndex) {
        return CROP_NAMES[cropIndex];
    }

    public Color getCropColor(int cropIndex) {
        switch (cropIndex) {
            case 0:
                return CARROT_COLOR;
            case 1:
                return TREE_COLOR;
            default:
                return Color.GRAY;
        }
    }

    // check if a crop can be planted on a tile based on the crop type and tile type
    public boolean canPlantOn(int cropIndex, TileManager tileManager, int row, int col) {
        if (cropIndex == 1) {
            return tileManager.isGrassTile(row, col) || tileManager.isSoilTile(row, col);
        }
        return tileManager.isSoilTile(row, col);
    }

    public boolean hasCrop(int row, int col) {
        return tileCrops[row][col] >= 0;
    }

    public String getCropAt(int row, int col) {
        int cropIndex = tileCrops[row][col];
        // return the crop name if a crop is planted
        return cropIndex >= 0 ? CROP_NAMES[cropIndex] : null;
    }

    // plant crop and set it as seed
    public void plantCrop(int row, int col, int cropIndex) {
        int previousCrop = tileCrops[row][col];
        if (previousCrop == cropIndex){
            return;
        }
        tileCrops[row][col] = cropIndex;
        cropStages[row][col] = SEED_STAGE;
        growthTicks[row][col] = 0;
    }

    // plant full grown crop on world generation
    public void plantMatureCrop(int row, int col, int cropIndex) {
        if (hasCrop(row, col)) {
            return;
        }
        tileCrops[row][col] = cropIndex;
        cropStages[row][col] = MATURE_STAGE;
        growthTicks[row][col] = 0;
    }

    public boolean removeCrop(int row, int col) {
        int cropIndex = tileCrops[row][col];
        if (cropIndex < 0) {
            return false;
        }
        tileCrops[row][col] = -1;
        cropStages[row][col] = SEED_STAGE;
        growthTicks[row][col] = 0;
        return true;
    }

    public int getCropCount(int cropIndex) {
        return harvestedCropCounts[cropIndex];
    }

    public BufferedImage getCropImage(int cropIndex) {
        switch (cropIndex) {
            case 0:
                return carrotImage;
            case 1:
                return treeImage;
            default:
                return null;
        }
    }

    public int getCropStage(int row, int col) {
        return cropStages[row][col];
    }

    public boolean isMature(int row, int col) {
        return hasCrop(row, col) && getCropStage(row, col) == MATURE_STAGE;
    }

    public boolean harvestCrop(int row, int col) {
        if (!isMature(row, col)) {
            return false;
        }
        int cropIndex = tileCrops[row][col];
        removeCrop(row, col);
        harvestedCropCounts[cropIndex]++;
        notifyCountListeners();
        return true;
    }

    // update the growth stage of all crops on the grid
    public void update(TileManager tileManager) {
        for (int row = 0; row < tileCrops.length; row++) {
            for (int col = 0; col < tileCrops[row].length; col++) {
                if (tileCrops[row][col] < 0 || cropStages[row][col] >= MATURE_STAGE) {
                    continue;
                }
                growthTicks[row][col]++;
                int cropIndex = tileCrops[row][col];
                int ticksPerStage = getGrowthTicksPerStage(cropIndex, tileManager, row, col);
                if (growthTicks[row][col] >= ticksPerStage) {
                    growthTicks[row][col] = 0;
                    cropStages[row][col]++;
                }
            }
        }
    }

    private int getGrowthTicksPerStage(int cropIndex, TileManager tileManager, int row, int col) {
        double suitability = getAttributeSuitability(cropIndex, tileManager, row, col);
        double growthRange = SLOWEST_GROWTH_TICKS_PER_STAGE - FASTEST_GROWTH_TICKS_PER_STAGE;
        return (int) Math.round(SLOWEST_GROWTH_TICKS_PER_STAGE - growthRange * suitability);
    }

    private double getAttributeSuitability(int cropIndex, TileManager tileManager, int row, int col) {
        int[][] idealRanges;
        switch (cropIndex) {
            case 0:
                // carrots prefer warm / nutrientful soil and high moisture.
                idealRanges = new int[][] {{35, 70}, {60, 80}, {45, 85}};
                break;
            case 1:
                idealRanges = new int[][] {{30, 75}, {30, 70}, {35, 80}};
                break;
            default:
                return 0;
        }

        int[] attributes = {
            tileManager.getTemperature(row, col),
            tileManager.getMoisture(row, col),
            tileManager.getNutrients(row, col)
        };
        double suitabilityTotal = 0;
        for (int attribute = 0; attribute < attributes.length; attribute++) {
            int minimum = idealRanges[attribute][0];
            int maximum = idealRanges[attribute][1];
            if (attributes[attribute] < minimum) {
                suitabilityTotal += (double) attributes[attribute] / minimum;
            } else if (attributes[attribute] > maximum) {
                suitabilityTotal += (double) (100 - attributes[attribute]) / (100 - maximum);
            } else {
                suitabilityTotal++;
            }
        }
        return suitabilityTotal / attributes.length;
    }

    public void addCountListener(CountListener listener) {
        countListeners.add(listener);
    }

    public void draw(Graphics2D graphics, int row, int col, int x, int y, int tileSize) {
        int cropIndex = tileCrops[row][col];
        if (cropIndex < 0) {
            return;
        }

        // draw the crop image if it is mature
        if (cropStages[row][col] == MATURE_STAGE) {
            BufferedImage cropImage = getCropImage(cropIndex);
            if (cropImage != null) {
                Image scaledCrop = cropImage.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH);
                graphics.drawImage(scaledCrop, x, y, null);
                return;
            }
        }

        // draw a circle for the seedling
        int stage = cropStages[row][col];
        graphics.setColor(stage == SEED_STAGE ? new Color(110, 75, 35) : new Color(45, 150, 55));
        int size = stage == SEED_STAGE ? 8 : stage == SPROUT_STAGE ? 14 : 24;
        graphics.fillOval(x + (tileSize - size) / 2, y + (tileSize - size) / 2, size, size);
    }

    private void notifyCountListeners() {
        for (CountListener listener : countListeners) {
            listener.countsChanged();
        }
    }

}

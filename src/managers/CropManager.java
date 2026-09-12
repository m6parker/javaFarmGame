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
import src.entities.crops.Crop;

public class CropManager {
    public static final int CROP_COUNT = 2;
    public static final int WOOD_CROP_INDEX = 1;
    public static final int MATURE_STAGE = 2;
    private static final int FASTEST_GROWTH_TICKS_PER_STAGE = 120;
    private static final int SLOWEST_GROWTH_TICKS_PER_STAGE = 240;
    private static final String[] CROP_NAMES = {"carrot", "tree"};
    // text color in crop selection menu
    private static final Color CARROT_COLOR = new Color(235, 125, 45);
    private static final Color TREE_COLOR = new Color(34, 139, 34);

    private final Crop[][] tileCrops;
    private final int[] harvestedCropCounts = new int[CROP_COUNT];
    private final List<CountListener> countListeners = new ArrayList<>();
    private BufferedImage[] carrotStageImages;
    private BufferedImage[] treeStageImages;
    private BufferedImage treeImage;
    private BufferedImage stumpImage;

    public interface CountListener {
        void countsChanged();
    }

    // constructor
    public CropManager(int rows, int cols) {
        // create 2D array of crops
        tileCrops = new Crop[rows][cols];
        try {
            // load image for crop from file
            carrotStageImages = new BufferedImage[] {
                ImageIO.read(new File("img/crops/carrot_stage_1.png")),
                ImageIO.read(new File("img/crops/carrot_stage_2.png")),
                ImageIO.read(new File("img/crops/carrot_stage_3.png"))
            };
            treeImage = ImageIO.read(new File("img/crops/tree.png"));
            treeStageImages = new BufferedImage[] {
                ImageIO.read(new File("img/crops/tree_stage_1.png")),
                ImageIO.read(new File("img/crops/tree_stage_2.png")),
                treeImage
            };
            stumpImage = ImageIO.read(new File("img/foliage/stump.png"));
        } catch (IOException exception) {
            System.err.println("Could not load image: " + exception.getMessage());
        }
    }

    public String getCropName(int cropIndex) {
        return createCrop(cropIndex).getName();
    }

    public Color getCropColor(int cropIndex) {
        return createCrop(cropIndex).getColor();
    }

    // check if a crop can be planted on a tile based on the crop type and tile type
    public boolean canPlantOn(int cropIndex, TileManager tileManager, int row, int col) {
        if (cropIndex == 1) {
            return tileManager.isGrassTile(row, col) || tileManager.isSoilTile(row, col);
        }
        return tileManager.isSoilTile(row, col);
    }

    public boolean hasCrop(int row, int col) {
        return tileCrops[row][col] != null;
    }

    public String getCropAt(int row, int col) {
        Crop crop = tileCrops[row][col];
        return crop == null ? null : crop.getName();
    }

    // plant crop and set it as seed
    public void plantCrop(int row, int col, int cropIndex) {
        Crop previousCrop = tileCrops[row][col];
        if (previousCrop != null && previousCrop.getTypeIndex() == cropIndex){
            return;
        }
        Crop crop = createCrop(cropIndex);
        crop.resetForPlanting();
        tileCrops[row][col] = crop;
    }

    // plant full grown crop on world generation
    public void plantMatureCrop(int row, int col, int cropIndex) {
        if (hasCrop(row, col)) {
            return;
        }
        Crop crop = createCrop(cropIndex);
        crop.setMature(MATURE_STAGE);
        tileCrops[row][col] = crop;
    }

    public boolean removeCrop(int row, int col) {
        if (tileCrops[row][col] == null) {
            return false;
        }
        tileCrops[row][col] = null;
        return true;
    }

    public int getCropCount(int cropIndex) {
        return harvestedCropCounts[cropIndex];
    }

    public int getWoodCount() {
        return harvestedCropCounts[WOOD_CROP_INDEX];
    }

    public boolean consumeWood(int amount) {
        if (amount < 0 || harvestedCropCounts[WOOD_CROP_INDEX] < amount) {
            return false;
        }
        harvestedCropCounts[WOOD_CROP_INDEX] -= amount;
        notifyCountListeners();
        return true;
    }

    public BufferedImage getCropImage(int cropIndex) {
        return createCrop(cropIndex).getImage();
    }

    public int getCropStage(int row, int col) {
        return tileCrops[row][col].getStage();
    }

    public boolean isMature(int row, int col) {
        return hasCrop(row, col) && !tileCrops[row][col].isStump()
            && tileCrops[row][col].getStage() == MATURE_STAGE;
    }

    public boolean harvestCrop(int row, int col) {
        if (!isMature(row, col)) {
            return false;
        }
        Crop crop = tileCrops[row][col];
        int cropIndex = crop.getTypeIndex();
        if (cropIndex == 1) {
            crop.setStump(stumpImage);
        } else {
            removeCrop(row, col);
        }
        harvestedCropCounts[cropIndex] += cropIndex == WOOD_CROP_INDEX ? 2 : 1;
        notifyCountListeners();
        return true;
    }

    // update the growth stage of all crops on the grid
    public void update(TileManager tileManager) {
        for (int row = 0; row < tileCrops.length; row++) {
            for (int col = 0; col < tileCrops[row].length; col++) {
                Crop crop = tileCrops[row][col];
                if (crop == null || crop.getStage() >= MATURE_STAGE) {
                    continue;
                }
                int ticksPerStage = getGrowthTicksPerStage(crop, tileManager, row, col);
                crop.advanceGrowth(ticksPerStage, MATURE_STAGE);
            }
        }
    }

    private int getGrowthTicksPerStage(Crop crop, TileManager tileManager, int row, int col) {
        double suitability = getAttributeSuitability(crop, tileManager, row, col);
        double growthRange = SLOWEST_GROWTH_TICKS_PER_STAGE - FASTEST_GROWTH_TICKS_PER_STAGE;
        return (int) Math.round(SLOWEST_GROWTH_TICKS_PER_STAGE - growthRange * suitability);
    }

    private double getAttributeSuitability(Crop crop, TileManager tileManager,
            int row, int col) {
        int[] attributes = {
            tileManager.getTemperature(row, col),
            tileManager.getMoisture(row, col),
            tileManager.getNutrients(row, col)
        };
        double suitabilityTotal = 0;
        for (int attribute = 0; attribute < attributes.length; attribute++) {
            int[] idealRange = crop.getIdealAttributeRange(attribute);
            int minimum = idealRange[0];
            int maximum = idealRange[1];
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

    public int getCropLevel(int row, int col) {
        return tileCrops[row][col] == null ? 0 : tileCrops[row][col].getLevel();
    }

    public int getCropSize(int row, int col) {
        return tileCrops[row][col] == null ? 0 : tileCrops[row][col].getSize();
    }

    public boolean upgradeCrop(int row, int col) {
        Crop crop = tileCrops[row][col];
        if (crop == null) {
            return false;
        }
        crop.upgrade();
        return true;
    }

    public void draw(Graphics2D graphics, int row, int col, int x, int y, int tileSize) {
        Crop crop = tileCrops[row][col];
        if (crop == null) {
            return;
        }

        BufferedImage cropImage = crop.isStump() ? crop.getImage() : crop.getStageImage();
        if (cropImage == null) {
            return;
        }
        int cropSize = tileSize * crop.getSize();
        int cropX = x - (cropSize - tileSize) / 2;
        int cropY = y - (cropSize - tileSize) / 2;
        Image scaledCrop = cropImage.getScaledInstance(
            cropSize, cropSize, Image.SCALE_SMOOTH);
        graphics.drawImage(scaledCrop, cropX, cropY, null);
    }

    private void notifyCountListeners() {
        for (CountListener listener : countListeners) {
            listener.countsChanged();
        }
    }

    private Crop createCrop(int cropIndex) {
        switch (cropIndex) {
            case 0:
                return new Crop(cropIndex, CROP_NAMES[cropIndex], CARROT_COLOR,
                    carrotStageImages[MATURE_STAGE], carrotStageImages,
                    new int[][] {{35, 70}, {60, 80}, {45, 85}});
            case 1:
                return new Crop(cropIndex, CROP_NAMES[cropIndex], TREE_COLOR, treeImage,
                    treeStageImages,
                    new int[][] {{30, 75}, {30, 70}, {35, 80}});
            default:
                throw new IllegalArgumentException("Unknown crop index: " + cropIndex);
        }
    }

}

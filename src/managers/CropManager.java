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
    public static final int CROP_COUNT = 1;
    private static final String[] CROP_NAMES = {"carrot"};
    // text color in crop selection menu
    private static final Color CARROT_COLOR = new Color(235, 125, 45);

    private final int[][] tileCrops;
    private final int[] cropCounts = new int[CROP_COUNT];
    private final List<CountListener> countListeners = new ArrayList<>();
    private BufferedImage carrotImage;

    public interface CountListener {
        void countsChanged();
    }

    // constructor
    public CropManager(int rows, int cols) {
        // create 2D array of crops
        tileCrops = new int[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                tileCrops[row][col] = -1;
            }
        }
        try {
            // load image for crop from file
            carrotImage = ImageIO.read(new File("img/carrot.png"));
        } catch (IOException exception) {
            System.err.println("Could not load img/carrot.png: " + exception.getMessage());
        }
    }

    public String getCropName(int cropIndex) {
        return CROP_NAMES[cropIndex];
    }

    public Color getCropColor(int cropIndex) {
        return CARROT_COLOR;
    }

    public boolean hasCrop(int row, int col) {
        return tileCrops[row][col] >= 0;
    }

    public String getCropAt(int row, int col) {
        int cropIndex = tileCrops[row][col];
        return cropIndex >= 0 ? CROP_NAMES[cropIndex] : null;
    }

    public void plantCrop(int row, int col, int cropIndex) {
        int previousCrop = tileCrops[row][col];
        if (previousCrop == cropIndex) {
            return;
        }
        if (previousCrop >= 0) {
            cropCounts[previousCrop]--;
        }
        tileCrops[row][col] = cropIndex;
        cropCounts[cropIndex]++;
        notifyCountListeners();
    }

    public boolean removeCrop(int row, int col) {
        int cropIndex = tileCrops[row][col];
        if (cropIndex < 0) {
            return false;
        }
        tileCrops[row][col] = -1;
        cropCounts[cropIndex]--;
        notifyCountListeners();
        return true;
    }

    public int getCropCount(int cropIndex) {
        return cropCounts[cropIndex];
    }

    public BufferedImage getCropImage(int cropIndex) {
        return carrotImage;
    }

    public void addCountListener(CountListener listener) {
        countListeners.add(listener);
    }

    public void draw(Graphics2D graphics, int row, int col, int x, int y, int tileSize) {
        int cropIndex = tileCrops[row][col];
        if (cropIndex < 0) {
            return;
        }

        if (carrotImage != null) {
            Image scaledCarrot = carrotImage.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH);
            graphics.drawImage(scaledCarrot, x, y, null);
        }
    }

    private void notifyCountListeners() {
        for (CountListener listener : countListeners) {
            listener.countsChanged();
        }
    }

}

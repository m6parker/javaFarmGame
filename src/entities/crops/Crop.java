package src.entities.crops;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Crop {
    private final int typeIndex;
    private final String name;
    private final Color color;
    private final BufferedImage image;
    private final int[][] idealAttributeRanges;
    private int level;
    private int size;
    private int stage;
    private int growthTicks;

    public Crop(int typeIndex, String name, Color color, BufferedImage image,
            int[][] idealAttributeRanges) {
        this.typeIndex = typeIndex;
        this.name = name;
        this.color = color;
        this.image = image;
        this.idealAttributeRanges = idealAttributeRanges;
        this.level = 1;
        this.size = 1;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getLevel() {
        return level;
    }

    public int getSize() {
        return size;
    }

    public int getStage() {
        return stage;
    }

    public int getGrowthTicks() {
        return growthTicks;
    }

    public int[] getIdealAttributeRange(int attribute) {
        return idealAttributeRanges[attribute].clone();
    }

    public void upgradeLevel() {
        level++;
    }

    public void upgradeSize() {
        size++;
    }

    public void upgrade() {
        upgradeLevel();
        upgradeSize();
    }

    public void resetForPlanting() {
        stage = 0;
        growthTicks = 0;
    }

    public void setMature(int matureStage) {
        stage = matureStage;
        growthTicks = 0;
    }

    public void advanceGrowth(int ticksPerStage, int matureStage) {
        if (stage >= matureStage) {
            return;
        }
        growthTicks++;
        if (growthTicks >= ticksPerStage) {
            growthTicks = 0;
            stage++;
        }
    }
}
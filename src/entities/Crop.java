package src.entities;
import java.awt.image.BufferedImage;

public class Crop {
    private final int typeIndex;
    private final String name;
    private BufferedImage image;
    private final BufferedImage[] stageImages;
    private final int[][] idealAttributeRanges;
    private int level;
    private int size;
    private int stage;
    private int growthTicks;
    private boolean stump;

    public Crop(int typeIndex, String name, BufferedImage image, BufferedImage[] stageImages, int[][] idealAttributeRanges) {
        this.typeIndex = typeIndex;
        this.name = name;
        this.image = image;
        this.stageImages = stageImages;
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

    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage getStageImage() {
        return stage >= 0 && stage < stageImages.length ? stageImages[stage] : null;
    }

    public boolean isStump() {
        return stump;
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

    public void setStump(BufferedImage stumpImage) {
        image = stumpImage;
        stump = true;
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
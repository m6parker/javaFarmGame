package src.entities;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class LilyPad {
    private static final int VARIANT_COUNT = 2;
    private static final BufferedImage[] VARIANT_IMAGES = loadVariantImages();

    private final int row;
    private final int col;
    private final BufferedImage image;

    public LilyPad(int row, int col, int variant) {
        this.row = row;
        this.col = col;
        this.image = VARIANT_IMAGES[variant];
    }

    public boolean isAt(int row, int col) {
        return this.row == row && this.col == col;
    }

    public static void removeAt(List<LilyPad> lilyPads, int row, int col) {
        lilyPads.removeIf(lilyPad -> lilyPad.isAt(row, col));
    }

    public void draw(Graphics2D graphics, int tileSize) {
        if (image == null) {
            return;
        }
        Image scaledImage = image.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH);
        graphics.drawImage(scaledImage, col * tileSize, row * tileSize, null);
    }

    private static BufferedImage[] loadVariantImages() {
        BufferedImage[] images = new BufferedImage[VARIANT_COUNT];
        for (int variant = 0; variant < VARIANT_COUNT; variant++) {
            try {
                images[variant] = ImageIO.read(
                        new File("img/foliage/lilypad_" + (variant + 1) + ".png"));
            } catch (IOException exception) {
                System.err.println("Could not load lily pad image: "
                        + exception.getMessage());
            }
        }
        return images;
    }
}
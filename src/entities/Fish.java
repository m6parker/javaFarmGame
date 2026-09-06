package src.entities;
import java.awt.Color;
import java.awt.Graphics2D;
import src.managers.TileManager;

public class Fish extends Mob {
    public Fish(int startCol, int startRow, int tileSize, int maxCol, int maxRow,
            TileManager tileManager) {
        super(startCol, startRow, tileSize);
    }

    @Override
    // Override the canEnterTile method to allow movement only on blue tiles
    protected boolean canEnterTile(Color tileColor) {
        return tileColor.equals(new Color(66, 135, 245));
    }

    @Override
    public void draw(Graphics2D graphics, int tileSize) {
        graphics.setColor(Color.ORANGE);
        int size = 10;
        graphics.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        graphics.setColor(Color.RED);
        graphics.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
    }
}

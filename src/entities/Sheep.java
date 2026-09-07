package src.entities;
import java.awt.Color;
import java.awt.Graphics2D;
import src.managers.TileManager;

public class Sheep extends Mob {
    public Sheep(int startCol, int startRow, int tileSize, int maxCol, int maxRow,
            TileManager tileManager) {
        super(startCol, startRow, tileSize);
    }

    @Override
    // Override the canEnterTile method to allow movement only on grass or soil tiles
    protected boolean canEnterTile(Color tileColor) {
        return tileColor.equals(TileManager.GRASS_COLOR) || tileColor.equals(TileManager.SOIL_COLOR);
    }

    @Override
    public void draw(Graphics2D graphics, int tileSize) {
        graphics.setColor(Color.WHITE);
        int size = 15;
        graphics.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        graphics.setColor(Color.GRAY);
        graphics.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
    }
}

package src.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import src.managers.TileManager;

public class Dweller extends Mob {
    public Dweller(int startCol, int startRow, int tileSize, int maxCol, int maxRow,
            TileManager tileManager) {
        super(startCol, startRow, tileSize);
    }

    @Override
    protected boolean canEnterTile(Color tileColor) {
        return tileColor.equals(TileManager.GRASS_COLOR)
                || tileColor.equals(TileManager.SOIL_COLOR);
    }

    @Override
    public void draw(Graphics2D graphics, int tileSize) {
        graphics.setColor(new Color(65, 105, 225));
        int size = 12;
        graphics.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        graphics.setColor(Color.BLACK);
        graphics.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
    }
}